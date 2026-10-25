"""Build the offline operation manual from verified UI screenshots, without external assets."""
from pathlib import Path
from html import escape
import base64
import hashlib
import json
import shutil

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT/'docs/operation-manual'
OUTPUT = ROOT.parent/'docs/操作手册'
SOURCE.mkdir(parents=True, exist_ok=True)
shutil.copytree(OUTPUT/'images', SOURCE/'images', dirs_exist_ok=True)
for p in (SOURCE/'images').glob('*.png'):
    if p.read_bytes().startswith(b'\xff\xd8'):
        target = p.with_suffix('.jpg')
        if target.exists():
            assert target.read_bytes() == p.read_bytes(), p.name
            p.unlink()
        else:
            p.rename(target)

sections = []
def section(key, title, body):
    sections.append((key, title, body))

def figure(name, caption):
    path = next((p for ext in ['jpg', 'png'] if (p := SOURCE/'images'/f'{name}.{ext}').is_file()), None)
    assert path, name
    src = 'images/'+path.name
    return f'<figure><a href="{src}" target="_blank"><img src="{src}" alt="{escape(caption)}"></a><figcaption>{escape(caption)}。点击图片可查看原图。</figcaption></figure>'

section('start', '登录与账号安全', '''
<p>打开学校提供的平台地址，输入工号和密码登录。新建账号的初始密码为工号；首次登录或管理员重置密码后，须按提示修改密码，之后再进入业务页面。强制改密窗口可返回登录界面。</p>
<ol><li>在登录页输入工号和现有密码，点击“登录”。</li><li>出现修改密码提示时，按页面规则填写新密码和确认密码并保存。</li><li>忘记密码可从登录页“重置密码”按提示处理；仍无法登录时联系管理员。管理员重置后，使用工号作为密码登录并重新改密。</li></ol>
<p>菜单和按钮根据账号当前权限显示。领导、专家、财务及普通填报人员看到的入口可能不同。退出或换用账号时，从右上角个人菜单退出，避免混用会话。</p>
''' + figure('login', '登录界面'))

section('search', '基础筛选与高级筛选', '''
<p>各列表默认保留常用的三至四个条件。更多条件在“高级筛选”中按归属、人员、状态和时间分组。不同页面提供与本业务有关的条件。</p>
<ol><li>先选择年度、月份或部门等基础条件，再输入名称或编号。</li><li>需要组合查询时展开“高级筛选”，选择责任人、审核阶段、进度区间、日期范围等条件。</li><li>点击“查询”统一应用条件；关键词输入框也可按回车。出现“条件已修改，待查询”时，当前列表仍对应上一次查询。</li><li>“已生效”标签显示当前结果使用的条件；高级区收起后条件继续生效。删除可关闭的标签会立即重新查询。</li><li>“清空高级条件”保留基础条件；“重置全部”恢复页面默认年月并清除其余条件。</li></ol>
<p>同一字段多选表示满足任一项，不同字段之间须同时满足。人员和任务候选可搜索、分批加载，已选值会保留。无目标值、未知时间等缺失值不按零值计算。</p>
<p>从详情返回时保留筛选和分页。首次加载显示加载状态；刷新时可能保留上一批结果并标注正在更新。失败时使用重试入口，旧结果不能视为已经更新。</p>
''' + figure('task-filters', '任务页面的基础条件与分组高级筛选'))

section('tasks', '任务填报与任务管理', '''
<p>入口：<b>工作中心 → 任务</b>。按一级改革任务、二级目录、三级任务展开查看。可按年度、归口部门、状态、编号或名称查询，再用高级筛选定位重点、困难、逾期或待审任务。</p>
<ol><li>展开目录，在三级任务行点击“查看”，阅读目标、责任人、处理人和完成情况。</li><li>有填报职责的账号按要求填写本次完成值、完成说明和材料，再提交反馈。含四级目标时须完整填写有效目标。</li><li>查看审核流程及日志，按当前状态进行退回修改、重新提交或符合条件的撤回。</li><li>管理员可在原页面新增任务、批量移交或执行符合条件的完结操作；移交需核对人员和影响，不能直接篡改进度。</li></ol>
<p><b>重点和困难：</b>同一任务可以同时具有两种标记。管理员在三级任务抽屉的“基本信息与完成情况”点击“修改管理属性”，修改标记及计划完成日期，填写原因，预览后确认保存。计划日期与原结束日期独立；实际完成日期只读，缺少可靠历史证据时留空。</p>
<p><b>四级目标汇总：</b>各目标按自身完成率等权计算，单项目标最多按100%计入三级任务。实际完成率可超100%，真实填报值保留。150%、50%、100%计入后平均为83.33%，列表整数进度为83%；只有达到完成条件并最终归档才为已完成。</p>
<p>树表滚动只改变可见行，不截断获准数据；跨视口勾选和批量操作仍作用于完整的当前筛选结果。宽表可在表格内横向滚动查看字段。</p>
''' + figure('tasks', '任务树中的状态、重点和困难标记') + figure('task-details', '任务详情中的管理属性与流程信息'))

section('performance', '绩效查看与填报', '''
<p>入口：<b>工作中心 → 绩效</b>。选择年度，按归口部门、达标情况、编号或名称查询。高级筛选可按责任人、指标分类、手动或自动、审核阶段、关联任务和年度达成比例进一步定位。</p>
<p>自动绩效根据既有任务关联计算，在详情查看关联任务与年度完成情况；手动绩效由有职责的人员填报并送审。管理员可按授权使用批量移交、关联编辑等功能。关联变更前查看预计重算结果并填写原因。</p>
<p>“数值达标”和“审核状态”分别展示，达到年度目标不等于已经归档。不同单位的指标不能直接相加；目标或完成值缺失时按待核实处理。</p>
<p>十大核心指标有独立填报入口。绩效完成值和核心指标完成值分别保存，填写其中一处不会自动代替另一处。</p>
''')

section('core', '十大核心指标独立填报', '''
<p>入口：<b>工作中心 → 核心指标</b>，预算页也提供直达按钮。页面用一张表展示十项指标、期满目标、最近归档完成值和填报状态。第十项为“发布《工程机械产业发展报告》数量”；如显示映射待核实，请联系管理员处理配置。</p>
<ol><li>选择填报年度和月份。可补填历史月份，不允许未来月份。</li><li>点击指标行“填报”，输入截至上年年末累计完成值、截至本次累计完成值和工作说明。工作说明最多500字，不上传附件。</li><li>尚未完成可“保存草稿”，之后仍从“填报”继续；确认后点击“提交审核”。</li><li>流程为专业群填报 → 对应绩效的归口审核人审核 → 双高办归档。退回后由本次提交人修改并重新提交，仍经过完整流程。</li><li>仅本次提交人可撤回尚无审核动作的待审记录。管理员按自身权限操作并记录真实操作者。</li></ol>
<p>专业群采用所选年度三级任务或四级目标责任人口径；管理员及双高办也可填报。同一指标同一月份最多存在一条在途审核，冲突时按提示刷新。</p>
<p>点击行首箭头查看跨月份最近两次正式填报；“历史记录”保留完整正式记录。未归档的新提交不会替代最近一次已归档结果；从未归档显示空值。所选月份状态按该月记录判断，不能把上月提交当成本月已填报。</p>
''' + figure('core-indicators', '核心指标表格汇总') + figure('core-fill', '核心指标填报字段与草稿提交入口'))

section('records', '工作纪实填报与导出', '''
<p>入口：<b>工作中心 → 工作纪实</b>，与任务、绩效等并列。每人每月一份，已有记录时直接打开。</p>
<p><b>填报范围：</b>所选年度三级任务或四级目标责任人视作专业群，可填报全校任务。归口部门的正常账号，只要本部门出现在某个一级任务的下级任务中，就可填报该一级任务下全部子任务；同时具有专业群身份时按全校范围。管理员可直接填报。</p>
<ol><li>点击“新建纪实”，选择年度、月份并确定。</li><li>在“基本情况”检查任务范围和统计，草稿可点击“刷新统计”。统计按三级任务去重，四级目标不重复计数。</li><li>在“主要工作亮点和成果”选择已有一级改革任务，点击“添加任务”逐项增加，再分别填写关键进展、阶段性成果、典型做法。添加条目不改变上方统计范围。</li><li>按需要填写存在问题、下阶段工作重点和其它事项，遵循每项字数提示。</li><li>保存草稿后可继续修改；确认后“提交纪实”。已提交记录及统计保留提交时版本。</li></ol>
<p>他人草稿始终仅本人可见。已提交纪实可按权限查看、单份导出Word或勾选批量导出。管理员删除须填写原因，执行逻辑删除；删除后允许原填报人重新填报同一月份，原删除记录保留。</p>
''' + figure('work-records', '工作纪实列表') + figure('work-record-edit', '纪实的基本情况与统计') + figure('work-record-entries', '逐项添加一级改革任务及文字内容'))

section('achievements', '建设成果与驳回处理', '''
<p>入口：<b>工作中心 → 成果</b>。默认按取得年度、报送部门、审核状态和名称查询；高级条件包括类别、级别、报送人、材料、日期和驳回原因等。</p>
<ol><li>有报送权限的人员点击“新增”，填写类别、名称、取得日期、奖项及材料等信息，按页面要求提交。</li><li>长成果名称显示可容纳的部分，鼠标悬停在名称处可滚动查看全名；进入详情可阅读完整内容。</li><li>已驳回成果仍保留展示，并标注“已驳回”和原因。查看说明后，按可用操作修改并重新提交。</li><li>在详情或审核入口查看提交、专业群审核及归档记录。历史记录缺少证据时显示未保存或不可判定，不据此推测审核经过。</li></ol>
<p>专业群审核与双高办归档分别留痕。统计与导出使用获准数据范围；成果条数和奖项数量是不同口径。删除按钮及审批按钮仅向有权限的账号开放。</p>
''' + figure('achievements', '成果名称、归档状态及驳回原因展示'))

section('budget', '预算月报与版本记录', '''
<p>入口：<b>工作中心 → 预算</b>。管理员、双高办及财务可按有效权限填报或导入，其他获准人员浏览。选择年度、月份查看对应整张预算表；金额单位为万元。</p>
<ol><li>需要导入时先下载模板，按选定年月及导入类型准备Excel文件。</li><li>点击“导入”，核对文件和月份后“确认导入并锁定”。需要编辑时，按权限解锁、修改，再锁定。</li><li>点击“版本记录”，按年月、导入人和时间查找历史版本，进入预览后再决定是否恢复。</li><li>恢复历史版本须填写原因，并生成一个新的当前版本，原历史版本不删除、不覆盖。</li><li>“整表导出”导出该月整张预算表。页面筛选只调整展示，不改变导入、锁定、恢复和导出的整表范围，也不改变年度执行率分母。</li></ol>
<p>预算中的核心指标按所选年月读取同年、不晚于该月份的最近有效归档结果，并显示所属月份。未归档留空；旧Excel中的核心指标文字只保留为原始导入信息，不自动成为审核通过记录。</p>
<p>原预算页面继续隐藏逐月折线图；趋势分析在综合看板中查看。缺月不补值，不跨缺月计算新增支出；负增长可能是财务调账。</p>
''' + figure('budget', '预算表与核心指标直达入口') + figure('budget-versions', '预算版本的查询与追溯入口'))

section('audits', '消息 审核与督办', '''
<p><b>消息中心：</b>按已读或未读、类型、发送时间和关键词查询。打开消息详情后，有处理职责的人员可前往业务。已经处理的记录按现状只读查看；无有效关联的信息按提示处理，不再把旧消息当成仍待审核的业务。</p>
<p><b>审核中心：</b>待办与审批记录分别查看，按业务类型、提交部门、提交人和编号或名称查询。先检查完成值、材料和历史意见，再通过、退回或执行归档；连续处理遵循当前生效筛选和下一条仍可办理的记录。侧栏数量随业务操作刷新。</p>
<p><b>督办中心：</b>按年度、部门、状态、任务名称查询，可在高级筛选中组合重点、困难、逾期天数、进度、处理人和计划日期。进入原任务详情查看进展，再执行有权限的督办操作。</p>
<p>领导仅增加查阅能力，不因此取得审批职责。消息仍限本人；没有实际审核职责时不开放处理入口。功能授权与具体业务处理人是两个条件，只有二者满足才能办理。</p>
''')

section('expert', '专家评审与意见修订', '''
<p>入口：<b>专家评审</b>。专家通常登录后直接进入本页，只能查看当前获准任务，按年度、一级任务、本人评审状态、编号或名称查询。</p>
<ol><li>一级任务点击“总体评语”，三级任务点击“专家评语”。抽屉打开后直接定位评语框；点击“任务详情”则从任务完成情况开始查看。</li><li>在同一抽屉内阅读目标、完成值、材料和审核历史，再填写评语。可用“查看任务完成情况”和“专家评语”在上下区域间定位。</li><li>查看“本次评审范围”。只获准部分任务时，总体意见仅针对列出的获准三级任务，不代表完整一级任务结论。</li><li>每份评语最多2000字，可保存草稿、正式提交、修改后重新提交。</li><li>“正式提交与修订历史”保留历次正式版本。修改草稿不会覆盖上一份正式意见，重新提交后才形成新版本。</li></ol>
<p>评语仅本人和管理员可见，不进入任务审批，不修改任务进度。草稿仅本人可见；教师、领导和其他专家不能查看他人的评语及数量。任务范围撤销、停用或任务删除后，专家不能继续访问，历史记录仍保留供管理员查阅。</p>
<p>管理员可在“正式意见查询”按专家、任务、年度筛选正式意见并导出，勾选是否包含修订历史。需要代填时显式选择评审身份，记录中保留实际管理员和代填标识；默认以管理员本人身份评审。</p>
''' + figure('expert-list', '专家评审对象和本人状态') + figure('expert-editor', '任务抽屉中的专家评语与修订入口'))

section('overview', '综合数据看板', '''
<p>入口：<b>管理中心 → 管理概览</b>，向管理员和已配置的校级领导开放。顶部选择年度、月份并刷新，确认当前查阅范围和更新时间。</p>
<ol><li>首页按任务、绩效、核心指标、成果、预算、工作纪实六类展示统计。</li><li>点击统计数字打开分析抽屉，查看说明、图表和分页明细。</li><li>切换部门、类别或状态等适用维度；点击图表分组或分组按钮筛选下方明细。</li><li>从明细进入原业务详情；清除分组标签恢复完整分析。首页卡片的原口径不随明细分页改变。</li><li>展开下方“运行维护”，查看账号、配置异常、待办异常及预算版本；领导保持只读。</li></ol>
<p>任务和绩效为所选年度当前状态，不是历史月末快照。任务完成率按已完成数量占比，平均推进进度另算；重点、困难可重叠。核心指标只取截至所选月的有效归档。成果按取得年份、当前状态统计，奖项数量单列。预算按所选月有效版本；未导入明确提示。纪实分子和分母采用同一当前有资格人员集合，不泄露草稿。</p>
<p>预算逐月折线图仅在本看板的预算分析中开放。模块加载失败会单独提示重试；“—”、未导入和失败不等于数值0。</p>
''' + figure('overview', '综合看板中的分类统计') + figure('overview-analysis', '统计数字展开的图表与分页明细'))

section('accounts', '账号与组织管理', '''
<p>入口：<b>管理中心 → 账号与组织</b>，仅管理员可维护。</p>
<ol><li>用户管理支持姓名或工号、部门、角色模板和启停状态查询；高级筛选可定位专家、领导、强制改密、个人授权和活动职责等账号。</li><li>新增普通账号时填写工号、姓名、账号名称、邮箱、部门和角色。工号创建后不修改，初始密码固定为工号并强制改密。</li><li>“新增专家账号”可创建不绑定校内部门的数字账号，再通过“配置权限”检查任务范围。</li><li>重置密码须填写原因。启停、降级和部门调整前核对当前业务影响，不能停用最后一个正常管理员。</li><li>部门管理仅维护负责人和启停状态。负责人候选为本部门正常账号，更换负责人不会自动重写历史任务或绩效审核人；需要时使用批量移交。</li></ol>
<p>存在活动任务、审核或其它职责时，系统会阻止停用并提示处理。并发修改提示冲突时，先刷新后重新核对，不能强行覆盖。</p>
''' + figure('accounts', '账号列表与专家账号 权限配置入口'))

section('permissions', '角色与个人权限', '''
<p>入口：<b>管理中心 → 角色与权限</b>，也可从账号行“配置权限”进入。管理员始终保留全权限；既有账号可保留原业务资格，也可按页面设置使用指定角色。</p>
<ol><li>在“角色模板”查看、新增、复制或修改模板，选择功能和模块数据范围。正在被人员使用的角色不能直接删除。</li><li>在“个人授权”搜索账号，选择角色，再对功能设为继承角色、允许或禁止。</li><li>角色允许项合并，个人明确设置覆盖角色。个人自定义数据范围替换该模块的角色范围。</li><li>任务范围可按全校、部门、一级或二级目录、指定三级任务及年度组合；部门和目录动态包含后续移入或新增任务，指定任务按固定ID。</li><li>查看“当前有效权限”、来源和“已有业务职责”，填写变更原因后预览影响，再确认保存。</li></ol>
<p>功能授权不会自动指定填报人或审核人。具体职责仍在任务、绩效配置和移交中维护。收紧权限会造成已有待办无法处理时，须先移交再保存；新增、移交和流程流转也会核对拟定处理人权限。</p>
<p>领导查阅范围可在账号管理中配置；当前校级领导暂按全校查阅处理，后续由管理员按实际分管调整。领导查阅不授予管理员写权限。其他角色的范围和功能以“当前有效权限”为准，旧Token不能保留已经撤销的授权。</p>
<p>管理员直接具有业务填报及审核能力，无需开启“管理员测试”按钮。对正式数据的管理员操作仍会产生真实记录，不是演练模式；操作记录保存真实操作者，专家代填另行显式选择身份。</p>
''' + figure('roles', '内置与自定义角色模板') + figure('personal-permissions', '个人功能 范围及当前有效权限'))

section('logs', '操作记录与常见问题', '''
<p>入口：<b>管理中心 → 操作记录</b>。按模块、操作人、时间、对象关键词查询，使用高级筛选定位动作、对象编号、批次及原因。打开详情查看变更前后内容；按已有导出入口导出获准记录。</p>
<p>账号、部门、任务管理、移交、绩效关联、预算导入或恢复、纪实删除和权限配置等操作按已有记录统一展示。未保存的历史动作不会补造；日志不提供删除入口，不展示密码和Token。</p>
<dl><dt>找不到菜单或无法查看某条任务</dt><dd>先确认登录账号与年度，再请管理员检查有效功能、数据范围及实际业务职责。部门和专家范围不能通过直接链接绕过。</dd><dt>筛选已改但列表没变</dt><dd>检查是否显示“条件已修改，待查询”，点击查询；同时检查已生效标签和收起的高级条件。</dd><dt>显示旧结果或页面还在加载</dt><dd>以加载、更新中或失败提示为准。等待请求结束或使用重试，不把旧结果当作本次查询结果，也不要连续重复提交。</dd><dt>已达标但仍审核中</dt><dd>数值达标与最终归档是不同状态，查看当前处理环节和审核记录。</dd><dt>工作纪实统计与当前任务列表不一致</dt><dd>先核对所选年度和一级任务范围；已提交纪实保留提交时快照。缺少可靠归档时间的历史任务会提示待核实，不补造完成日期。</dd><dt>预算或核心指标出现空白</dt><dd>可能是所选月未导入、尚未有效归档或缺少有效分母；不会引用未来月份，也不应把空白当作0。</dd><dt>更新后仍显示旧页面</dt><dd>在无未保存内容时强制刷新浏览器；仍异常时向管理员提供页面地址和截图，由管理员核对发布版本。</dd></dl>
''')

css = '''*{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:#f3f5f8;color:#243042;font:16px/1.85 "Microsoft YaHei","PingFang SC",sans-serif}a{color:#165ea8;text-decoration:none}a:hover{text-decoration:underline}aside{position:fixed;inset:0 auto 0 0;width:248px;overflow:auto;background:#0c2239;padding:30px 22px;color:#fff}aside strong{font-size:20px}aside small{display:block;color:#b2c5da;margin:10px 0 22px}aside a{display:block;color:#d3e0ef;font-size:14px;padding:8px 0;line-height:1.5}main{max-width:1200px;margin-left:248px;padding:40px 46px 80px}header,section,footer{background:white;padding:30px 36px;margin-bottom:22px;border:1px solid #e1e6ed;border-radius:8px}header{padding-top:36px}header .eyebrow{font-size:14px;color:#165ea8;margin:0}h1{font-size:29px;color:#000;line-height:1.45;margin:12px 0}h2{font-size:23px;color:#000;line-height:1.5;margin:0 0 20px;padding-bottom:12px;border-bottom:1px solid #dce3ec}p{margin:13px 0}li{margin:7px 0}ol{padding-left:25px}section{scroll-margin-top:18px}figure{margin:24px 0}img{display:block;width:100%;height:auto;border:1px solid #dde3eb;border-radius:4px}figcaption{font-size:13px;color:#627084;line-height:1.7;margin-top:9px}button{border:1px solid #165ea8;color:#165ea8;background:white;border-radius:5px;padding:10px 20px;cursor:pointer;font:inherit}table{width:100%;border-collapse:collapse;font-size:14px;margin:20px 0}th,td{border:1px solid #d9dfe7;padding:10px 12px;text-align:left;vertical-align:top}th{background:#eaf0f6;color:#111}dt{font-weight:bold;margin:20px 0 4px}dd{margin:0}footer{font-size:13px;color:#627084}.meta{font-size:14px;color:#627084}.print-only{display:none}@media(max-width:1000px){aside{position:static;width:auto;padding:18px 22px}aside nav{display:flex;flex-wrap:wrap;gap:4px 20px}aside a{font-size:13px}aside small{margin:6px 0}main{margin:0;padding:18px}section,header,footer{padding:24px}}@media print{@page{size:A4;margin:16mm}html{scroll-behavior:auto}body{background:white;font-size:10.5pt;line-height:1.7}aside,.no-print{display:none}main{padding:0;max-width:none;margin:0}header,section,footer{border:0;border-radius:0;margin:0;padding:0}header{margin-bottom:22pt}h1{font-size:22pt}h2{font-size:16pt;break-after:avoid;margin-top:24pt}p,li{orphans:3;widows:3}figure{break-inside:avoid;margin:16pt 0}img{max-height:170mm;object-fit:contain}a{color:inherit}figcaption{font-size:9pt}table{font-size:9pt}tr{break-inside:avoid}thead{display:table-header-group}.print-only{display:block}}'''
nav = ''.join(f'<a href="#{key}">{i}. {title}</a>' for i, (key, title, _) in enumerate(sections, 1))
body = ''.join(f'<section id="{key}"><h2>{i} {title}</h2>{content}</section>' for i, (key, title, content) in enumerate(sections, 1))
intro = '''<header><p class="eyebrow">徐州工业职业技术学院</p><h1>双高建设计划项目平台操作手册</h1><p class="meta">2026年10月25日更新 · 日常填报 审核 专家评审与管理</p><p>本手册按当前界面说明日常操作，补充核心指标、专家评审、角色与权限、综合看板、工作纪实范围和高级筛选。先按自己的业务入口阅读，再用目录定位具体步骤。</p><p>图片由内置浏览器在本地隔离演示环境截取，人员、任务和数值均为合成示例，不是线上统计结论。正式账号的可见内容以有效权限和当前职责为准。</p><table><thead><tr><th>使用人员</th><th>常用章节</th><th>要点</th></tr></thead><tbody><tr><td>任务与专业群填报人员</td><td>2—6、9</td><td>先查年度和范围，草稿确认后再提交</td></tr><tr><td>归口部门与双高办</td><td>5—9</td><td>按实际处理人办理，归档结果用于展示</td></tr><tr><td>财务人员</td><td>8</td><td>导入、恢复和导出均针对整张月度表</td></tr><tr><td>专家</td><td>10</td><td>在任务抽屉中评审，意见仅本人和管理员可见</td></tr><tr><td>校级领导</td><td>11</td><td>按查阅范围浏览，综合看板只读</td></tr><tr><td>管理员</td><td>11—14</td><td>维护账号、权限和职责，正式操作留痕</td></tr></tbody></table><button class="no-print" onclick="window.print()">打印或另存为 PDF</button></header>'''
html = '<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>双高平台操作手册 20261025</title><style>'+css+'</style></head><body><aside><strong>平台操作手册</strong><small>2026年10月25日图文版<br>离线打开 · 点击目录定位</small><nav>'+nav+'</nav></aside><main>'+intro+body+'<footer>适用版本 shuanggao-update-20261025。截图为演示环境；文中的角色能力还须满足账号有效权限及业务职责。技术部署步骤见更新包README，不在普通用户页面执行数据库操作。</footer></main></body></html>'
(SOURCE/'index.html').write_text(html, encoding='utf8')
standalone = html
for p in (SOURCE/'images').glob('*.jpg'):
    data = 'data:image/jpeg;base64,'+base64.b64encode(p.read_bytes()).decode('ascii')
    standalone = standalone.replace('src="images/'+p.name+'"', 'src="'+data+'"').replace('href="images/'+p.name+'"', 'href="'+data+'"')
(SOURCE/'offline.html').write_text(standalone, encoding='utf8')
(ROOT.parent/'docs/双高平台操作手册20261025.html').write_text(standalone, encoding='utf8')
images = sorted((SOURCE/'images').glob('*'))
write = {'version':'20261025','format':'offline-html','screenshots':'in-app browser; isolated synthetic data','sections':[{'id':key,'title':title} for key,title,_ in sections],'images':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in images if p.is_file()}}
(SOURCE/'manual-manifest.json').write_text(json.dumps(write, ensure_ascii=False, indent=2)+'\n', encoding='utf8')
shutil.copytree(SOURCE, OUTPUT, dirs_exist_ok=True)
for p in (OUTPUT/'images').glob('*.png'):
    other = p.with_suffix('.jpg')
    if other.exists() and p.read_bytes() == other.read_bytes():
        p.unlink()
print(json.dumps({'manual':str(OUTPUT/'index.html'),'sections':len(sections),'screenshots':len(write['images'])}, ensure_ascii=False))
