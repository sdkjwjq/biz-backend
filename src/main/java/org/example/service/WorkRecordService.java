package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.BizWorkRecord;
import org.example.entity.SysUser;
import org.example.entity.vo.WorkRecordVO.*;
import org.example.mapper.SysMapper;
import org.example.mapper.WorkRecordMapper;
import org.example.utils.BusinessLogUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkRecordService {
    private final WorkRecordMapper records;
    private final SysMapper users;
    private final WorkRecordStatisticsService statistics;
    private final ObjectMapper json;
    private final Clock clock;
    private final Set<Long> viewers;

    public WorkRecordService(WorkRecordMapper records, SysMapper users, WorkRecordStatisticsService statistics,
                             ObjectMapper json, @Qualifier("workRecordClock") Clock clock,
                             @Value("${work-records.viewer-user-ids:}") String viewerIds) {
        this.records = records;
        this.users = users;
        this.statistics = statistics;
        this.json = json;
        this.clock = clock;
        this.viewers = Arrays.stream(viewerIds.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .map(Long::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    private SysUser user(Long id) {
        SysUser user = users.getUserById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDelete())) throw new WorkRecordException(401, "请重新登录");
        return user;
    }

    public Capabilities capabilities(Long userId) {
        SysUser user = user(userId);
        List<Integer> years = records.fillableYears(userId);
        boolean export = "0".equals(user.getRole()) || viewers.contains(userId);
        return new Capabilities(!years.isEmpty(), export || !years.isEmpty(), export,
                records.ownSubmitted(userId) > 0, years);
    }

    private void requireFiller(Long userId, int year) {
        user(userId);
        if (!records.fillableYears(userId).contains(year)) throw new WorkRecordException(403, "您不是该年度任务的专业群审核人，不能填报或刷新纪实");
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Statistics preview(Long userId, int year, int month) {
        statistics.validatePeriod(year, month);
        requireFiller(userId, year);
        return statistics.calculate(userId, year, month);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Detail create(Long userId, int year, int month) {
        statistics.validatePeriod(year, month);
        BizWorkRecord existing = records.byMonth(userId, year, month);
        if (existing != null) return detail(userId, existing.getRecordId());
        requireFiller(userId, year);
        SysUser owner = user(userId);
        BizWorkRecord record = new BizWorkRecord();
        record.setOwnerId(userId);
        record.setOwnerName(owner.getNickName() == null || owner.getNickName().isBlank() ? owner.getUserName() : owner.getNickName());
        record.setRecordYear(year);
        record.setRecordMonth(month);
        record.setCreateTime(Date.from(clock.instant()));
        record.setUpdateTime(record.getCreateTime());
        records.create(record);
        BizWorkRecord stored = records.lock(record.getRecordId());
        // 唯一键并发命中时使用已存在记录，不覆盖另一个窗口的正文、版本和快照。
        if (records.lockSnapshot(stored.getRecordId()) == null) saveSnapshot(stored, statistics.calculate(userId, year, month));
        BusinessLogUtil.info("工作纪实新建", "userId", userId, "recordId", stored.getRecordId());
        return buildDetail(userId, stored, records.lockSnapshot(stored.getRecordId()));
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Detail detail(Long userId, Long id) {
        user(userId);
        BizWorkRecord record = records.byId(id);
        if (record == null || !(userId.equals(record.getOwnerId())
                || (Integer.valueOf(1).equals(record.getStatus()) && capabilities(userId).canViewAll()))) {
            throw new WorkRecordException(404, "纪实不存在或无权访问");
        }
        return buildDetail(userId, record);
    }

    private Detail buildDetail(Long userId, BizWorkRecord record) {
        return buildDetail(userId, record, records.snapshot(record.getRecordId()));
    }

    private Detail buildDetail(Long userId, BizWorkRecord record, String saved) {
        Statistics snapshot;
        try {
            if (saved == null) throw new IllegalStateException("Missing work record snapshot");
            snapshot = json.readValue(saved, Statistics.class);
        } catch (JsonProcessingException e) { throw new IllegalStateException("Invalid work record snapshot", e); }
        boolean editable = userId.equals(record.getOwnerId()) && Integer.valueOf(0).equals(record.getStatus())
                && records.fillableYears(userId).contains(record.getRecordYear());
        return new Detail(record, snapshot, editable);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Detail refresh(Long userId, Long id, long version) {
        user(userId);
        BizWorkRecord record = records.lock(id);
        if (record == null || !userId.equals(record.getOwnerId())) throw new WorkRecordException(404, "纪实不存在或无权访问");
        if (!Integer.valueOf(0).equals(record.getStatus())) throw new WorkRecordException(409, "已提交纪实不可修改");
        requireFiller(userId, record.getRecordYear());
        if (version != record.getVersion()) throw new WorkRecordException(409, "纪实已在其它窗口更新，请刷新后重试");
        Statistics result = statistics.calculate(userId, record.getRecordYear(), record.getRecordMonth());
        if (records.advanceVersion(id, userId, version, Date.from(clock.instant())) != 1) throw new WorkRecordException(409, "纪实版本已变更，请刷新后重试");
        saveSnapshot(record, result);
        BusinessLogUtil.info("工作纪实刷新统计", "userId", userId, "recordId", id, "version", version + 1);
        return buildDetail(userId, records.lock(id));
    }

    private void saveSnapshot(BizWorkRecord record, Statistics result) {
        try { records.saveSnapshot(record.getRecordId(), json.writeValueAsString(result), Date.from(clock.instant())); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Cannot serialize work record snapshot", e); }
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Page list(Long userId, Integer year, Integer month, Long ownerId, Integer status, int page, int pageSize) {
        Capabilities access = capabilities(userId);
        if ((year != null && (year < 2025 || year > 2029)) || (month != null && (month < 1 || month > 12))
                || (status != null && status != 0 && status != 1) || (ownerId != null && ownerId <= 0)
                || page < 1 || pageSize < 1 || pageSize > 100) throw new WorkRecordException(400, "列表筛选或分页参数不正确");
        long total = records.count(userId, access.canViewAll(), year, month, ownerId, status);
        List<BizWorkRecord> rows = records.list(userId, access.canViewAll(), year, month, ownerId, status,
                pageSize, (long) (page - 1) * pageSize);
        return new Page(total, page, pageSize, rows);
    }
}
