package org.example.service;

import org.example.utils.AdminTestContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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
    private final LeadershipAccessService leadershipAccess;
    private final WorkRecordScopeService scopes;
    @org.springframework.beans.factory.annotation.Autowired private PermissionService permissions;

    public WorkRecordService(WorkRecordMapper records, SysMapper users, WorkRecordStatisticsService statistics,
                             ObjectMapper json, @Qualifier("workRecordClock") Clock clock,
                             @Value("${work-records.viewer-user-ids:}") String viewerIds, LeadershipAccessService leadershipAccess,
                             WorkRecordScopeService scopes) {
        this.records = records;
        this.users = users;
        this.statistics = statistics;
        this.json = json;
        this.clock = clock;
        this.leadershipAccess = leadershipAccess;
        this.scopes = scopes;
        this.viewers = Arrays.stream(viewerIds.split(",")).map(String::trim).filter(value -> !value.isEmpty())
                .map(Long::valueOf).collect(Collectors.toUnmodifiableSet());
    }

    private SysUser user(Long id) {
        SysUser user = users.getUserById(id);
        if (user == null || Integer.valueOf(1).equals(user.getIsDelete()) || !"1".equals(user.getStatus())) throw new WorkRecordException(401, "请重新登录");
        return user;
    }

    private List<Integer> fillableYears(SysUser user) {
        return scopes.fillableYears(user);
    }

    public Capabilities capabilities(Long userId) {
        SysUser user = user(userId);
        List<Integer> years = fillableYears(user);
        if (permissions.enforced(userId)) {boolean fill=permissions.allowed(userId,"records.fill"),read=permissions.allowed(userId,"records.read"),others=read&&permissions.scopeRules(permissions.state(userId),"records").stream().anyMatch(r->!List.of("OWN","NONE").contains(String.valueOf(r.get("mode"))));return new Capabilities(fill&&!years.isEmpty(),others,permissions.allowed(userId,"records.export"),read&&records.ownSubmitted(userId)>0,fill?years:List.of(),read&&records.ownRecords(userId)>0,permissions.allowed(userId,"records.delete"));}
        boolean export = "0".equals(user.getRole()) || viewers.contains(userId) || leadershipAccess.isLeader(userId);
        return new Capabilities(!years.isEmpty(), export || !years.isEmpty(), export,
                records.ownSubmitted(userId) > 0, years, records.ownRecords(userId) > 0, "0".equals(user.getRole()));
    }

    /** 仅删除已提交纪实；正文、条目及冻结快照保留。 */
    @Transactional
    public Map<String, String> delete(Long userId, Long id, JsonNode body) {
        if (!"0".equals(user(userId).getRole())&&!(permissions.enforced(userId)&&permissions.allowed(userId,"records.delete"))) throw new WorkRecordException(403, "没有删除工作纪实权限");
        long expectedVersion = version(body);
        String reason = text(body, "reason").strip();
        if (reason.codePoints().noneMatch(cp -> !Character.isWhitespace(cp) && !Character.isSpaceChar(cp))) throw new WorkRecordException(400, "请填写删除原因");
        BizWorkRecord record = records.lock(id);
        if (record == null || (record.getStatus() != 1 && !userId.equals(record.getOwnerId()))) throw new WorkRecordException(404, "纪实不存在或无权操作");
        if (record.getStatus() != 1) throw new WorkRecordException(409, "仅支持删除已提交纪实");
        if (record.getVersion() != expectedVersion) throw new WorkRecordException(409, "纪实已变更，请刷新后重新确认删除");
        if (records.markDeleted(id, expectedVersion, userId, reason, Date.from(clock.instant())) != 1) throw new WorkRecordException(409, "纪实已变更，请刷新后重试");
        BusinessLogUtil.info("工作纪实删除", "userId", userId, "recordId", id, "ownerId", record.getOwnerId());
        return Map.of("message", "纪实已删除，填报人可重新填报该月份");
    }

    private void requireFiller(SysUser user, int year) {
        if (!fillableYears(user).contains(year)) throw new WorkRecordException(403, "该年度没有您的专业群或归口部门可填报任务，不能填报或刷新纪实");
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Statistics preview(Long userId, int year, int month) {
        statistics.validatePeriod(year, month);
        SysUser filler = user(userId);
        requireFiller(filler, year);
        return statistics.calculate(userId, year, month);
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Detail create(Long userId, int year, int month) {
        statistics.validatePeriod(year, month);
        BizWorkRecord existing = records.byMonth(userId, year, month);
        if (existing != null) return detail(userId, existing.getRecordId());
        SysUser owner = user(userId);
        requireFiller(owner, year);
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
        AdminTestContext.record(userId, "WORK_RECORD", stored.getRecordId(), stored.getRecordYear() + "年" + stored.getRecordMonth() + "月工作纪实（" + stored.getOwnerName() + "）", "新建草稿");
        return buildDetail(userId, stored, records.lockSnapshot(stored.getRecordId()));
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Detail detail(Long userId, Long id) {
        user(userId);
        BizWorkRecord record = records.byId(id);
        if (record == null || !(userId.equals(record.getOwnerId())
                || AdminTestContext.enabled(userId) || (Integer.valueOf(1).equals(record.getStatus()) && capabilities(userId).canViewAll()))) {
            throw new WorkRecordException(404, "纪实不存在或无权访问");
        }
        if(permissions.enforced(userId)&&!permissions.canRead(userId,"records",id,record.getRecordYear()))throw new WorkRecordException(404,"纪实不存在或无权访问");
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
        boolean editable = (userId.equals(record.getOwnerId()) || AdminTestContext.enabled(userId)) && Integer.valueOf(0).equals(record.getStatus())
                && fillableYears(user(userId)).contains(record.getRecordYear())
                && (!permissions.enforced(userId)||permissions.allowed(userId,"records.fill"));
        return new Detail(record, snapshot, editable, records.entries(record.getRecordId()));
    }

    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Detail refresh(Long userId, Long id, long version) {
        SysUser filler = user(userId);
        BizWorkRecord record = records.lock(id);
        if (record == null || (!userId.equals(record.getOwnerId()) && !AdminTestContext.enabled(userId))) throw new WorkRecordException(404, "纪实不存在或无权访问");
        if (!Integer.valueOf(0).equals(record.getStatus())) throw new WorkRecordException(409, "已提交纪实不可修改");
        requireFiller(filler, record.getRecordYear());
        if (version != record.getVersion()) throw new WorkRecordException(409, "纪实已在其它窗口更新，请刷新后重试");
        user(record.getOwnerId()); // 管理员代填也不能为已停用/删除的原填报人重写统计。
        Statistics result = statistics.calculate(record.getOwnerId(), record.getRecordYear(), record.getRecordMonth());
        if (records.advanceVersion(id, record.getOwnerId(), version, Date.from(clock.instant())) != 1) throw new WorkRecordException(409, "纪实版本已变更，请刷新后重试");
        saveSnapshot(record, result);
        BusinessLogUtil.info("工作纪实刷新统计", "userId", userId, "recordId", id, "version", version + 1);
        return buildDetail(userId, records.lock(id));
    }

    private void saveSnapshot(BizWorkRecord record, Statistics result) {
        try { records.saveSnapshot(record.getRecordId(), json.writeValueAsString(result), Date.from(clock.instant())); }
        catch (JsonProcessingException e) { throw new IllegalStateException("Cannot serialize work record snapshot", e); }
    }

    public List<Author> authors(Long userId) {
        if(permissions.enforced(userId)){List<Long> allowed=permissions.recordIds(userId);if(allowed.isEmpty())return List.of();return records.scopedAuthors(allowed);}
        return records.authors(userId, capabilities(userId).canViewAll(), AdminTestContext.enabled(userId));
    }

    /** 一次核验全部所选记录；任何记录无效都拒绝整批，禁止静默跳过。 */
    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public List<Detail> exportDetails(Long userId, List<Long> ids) {
        if (!capabilities(userId).canExport()) throw new WorkRecordException(403, "您没有纪实导出权限");
        if (ids == null || ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id <= 0)) throw new WorkRecordException(400, "请选择有效的纪实记录");
        List<Detail> result = new ArrayList<>();
        for (Long id : new LinkedHashSet<>(ids)) {
            Detail detail = detail(userId,id);
            if (!Integer.valueOf(1).equals(detail.record().getStatus())) throw new WorkRecordException(409, "所选纪实包含草稿，不能导出");
            result.add(detail);
        }
        result.sort(Comparator.comparing((Detail value) -> value.record().getRecordYear())
                .thenComparing(value -> value.record().getRecordMonth())
                .thenComparing(value -> value.record().getOwnerId()));
        return result;
    }

    /** 正文、条目、提交时统计快照在一个事务中保存，旧版本和已提交记录均不可写。 */
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Detail save(Long userId, Long id, JsonNode body, boolean submit) {
        SysUser owner = user(userId);
        BizWorkRecord record = records.lock(id);
        if (record == null || (!userId.equals(record.getOwnerId()) && !AdminTestContext.enabled(userId))) throw new WorkRecordException(404, "纪实不存在或无权访问");
        if (!Integer.valueOf(0).equals(record.getStatus())) throw new WorkRecordException(409, "已提交纪实不可修改");
        requireFiller(owner, record.getRecordYear());
        long version = version(body);
        if (version != record.getVersion()) throw new WorkRecordException(409, "纪实已在其它窗口更新，请重新加载后再操作；当前未保存内容仍保留");
        owner = user(record.getOwnerId());
        Statistics current = statistics.calculate(record.getOwnerId(), record.getRecordYear(), record.getRecordMonth());
        Map<Long, ReformTask> allowed = current.reformTasks().stream().collect(Collectors.toMap(ReformTask::taskId, value -> value));
        JsonNode items = body.get("entries");
        if (items == null || !items.isArray()) throw new WorkRecordException(400, "请提供改革任务填报项目");
        if (items.size() > allowed.size()) throw new WorkRecordException(400, "改革任务项目重复或超出可填报范围");
        Set<Long> used = new HashSet<>();
        List<Entry> entries = new ArrayList<>();
        for (JsonNode item : items) {
            JsonNode task = item.get("reformTaskId");
            if (task == null || !task.isIntegralNumber() || !task.canConvertToLong() || !allowed.containsKey(task.asLong())) {
                throw new WorkRecordException(400, "所选改革任务已不在您的填报范围内，请刷新统计后核对");
            }
            long taskId = task.asLong();
            if (!used.add(taskId)) throw new WorkRecordException(400, "同一改革任务不能重复填写");
            Entry entry = new Entry();
            entry.setRecordId(id); entry.setReformTaskId(taskId); entry.setReformTaskName(allowed.get(taskId).taskName());
            entry.setSortOrder(entries.size());
            entry.setKeyProgress(text(item,"keyProgress")); entry.setStageResults(text(item,"stageResults"));
            entry.setTypicalPractices(text(item,"typicalPractices")); entries.add(entry);
        }
        if (submit && entries.stream().noneMatch(entry -> hasContent(entry.getKeyProgress())
                || hasContent(entry.getStageResults()) || hasContent(entry.getTypicalPractices()))) {
            throw new WorkRecordException(400, "提交前请至少选择一项改革任务并填写有效内容");
        }
        record.setProblems(text(body,"problems")); record.setNextFocus(text(body,"nextFocus"));
        record.setOtherMatters(text(body,"otherMatters"));
        record.setOwnerName(owner.getNickName() == null || owner.getNickName().isBlank() ? owner.getUserName() : owner.getNickName());
        record.setUpdateTime(Date.from(clock.instant())); record.setStatus(submit ? 1 : 0);
        record.setSubmitTime(submit ? record.getUpdateTime() : null);
        if (records.saveBody(record) != 1) throw new WorkRecordException(409, "纪实版本已变更，请重新加载后重试");
        records.deleteEntries(id);
        for (Entry entry : entries) records.insertEntry(entry);
        if (submit) saveSnapshot(record, current);
        BusinessLogUtil.info(submit ? "工作纪实提交" : "工作纪实保存草稿", "userId", userId, "recordId", id, "version", version+1);
        AdminTestContext.record(userId, "WORK_RECORD", id, record.getRecordYear() + "年" + record.getRecordMonth() + "月工作纪实（" + record.getOwnerName() + "）", submit ? "提交" : "保存草稿");
        return buildDetail(userId, records.lock(id));
    }

    private boolean hasContent(String text) {
        return text.codePoints().anyMatch(value -> !Character.isWhitespace(value) && !Character.isSpaceChar(value));
    }

    private long version(JsonNode body) {
        JsonNode value = body == null ? null : body.get("version");
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong() || value.asLong() < 0) {
            throw new WorkRecordException(400, "请提供有效的纪实版本号");
        }
        return value.asLong();
    }

    private String text(JsonNode body, String field) {
        JsonNode value = body.get(field);
        if (value == null || value.isNull()) return "";
        if (!value.isTextual()) throw new WorkRecordException(400, "填报内容必须为文本");
        String content = value.asText();
        if (content.codePointCount(0,content.length()) > 300) throw new WorkRecordException(400, "每项填报内容不得超过300字");
        return content;
    }

    @Transactional(readOnly=true, isolation=Isolation.REPEATABLE_READ)
    public Page list(Long userId, Integer year, Integer month, Long ownerId, Integer status, int page, int pageSize) {
        Capabilities access = capabilities(userId);
        if ((year != null && (year < 2025 || year > 2029)) || (month != null && (month < 1 || month > 12))
                || (status != null && status != 0 && status != 1) || (ownerId != null && ownerId <= 0)
                || page < 1 || pageSize < 1 || pageSize > 100) throw new WorkRecordException(400, "列表筛选或分页参数不正确");
        if(permissions.enforced(userId)){List<Long> allowed=permissions.recordIds(userId);if(allowed.isEmpty())return new Page(0,page,pageSize,List.of());long total=records.scopedCount(allowed,year,month,ownerId,status);return new Page(total,page,pageSize,records.scopedList(allowed,year,month,ownerId,status,pageSize,(long)(page-1)*pageSize));}
        long total = records.count(userId, access.canViewAll(), AdminTestContext.enabled(userId), year, month, ownerId, status);
        List<BizWorkRecord> rows = records.list(userId, access.canViewAll(), AdminTestContext.enabled(userId), year, month, ownerId, status,
                pageSize, (long) (page - 1) * pageSize);
        return new Page(total, page, pageSize, rows);
    }
}
