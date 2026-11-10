<template>
  <div class="permission-scopes">
    <p class="muted">{{ personal ? '未指定时合并角色范围；指定后替换该模块的全部角色范围。' : '功能和范围需要同时授权。未设置范围的模块没有可查阅的数据。' }} 部门、目录和指定记录取并集，年度限制作用于该模块全部选择。</p>
    <el-tabs>
      <el-tab-pane v-for="(label, domain) in (personal ? modules : roleModules)" :key="domain" :label="label">
        <el-switch :model-value="!!modelValue[domain]" :active-text="personal ? '指定个人范围' : '设置角色范围'" @change="toggle(domain, $event)" />
        <template v-if="modelValue[domain]">
          <div class="scope-fields">
            <el-select :aria-label="`${label}范围类型`" :model-value="modelValue[domain].mode" @update:model-value="change(domain, 'mode', $event)">
              <el-option v-for="mode in modes(domain)" :key="mode" :value="mode" :label="modeNames[mode]" />
            </el-select>
            <el-select :aria-label="`${label}建设年度`" multiple clearable collapse-tags placeholder="全部建设年度" :model-value="modelValue[domain].years || []" @update:model-value="change(domain, 'years', $event)"><el-option v-for="year in years" :key="year" :label="`${year}年`" :value="year" /></el-select>
          </div>
          <template v-if="modelValue[domain].mode === 'CUSTOM'">
            <el-form label-position="top">
              <el-form-item v-if="!['core', 'expert'].includes(domain)" label="部门"><el-select :aria-label="`${label}部门范围`" multiple clearable filterable collapse-tags :model-value="modelValue[domain].departments || []" @update:model-value="change(domain, 'departments', $event)"><el-option v-for="dept in options.departments || []" :key="dept.id" :label="dept.name" :value="dept.id" :disabled="String(dept.status) === '1'" /></el-select></el-form-item>
              <el-form-item v-if="domain === 'tasks'" label="一级／二级目录（包含以后新增或移入的三级任务）"><RemoteCandidateSelect aria-label="任务目录范围" multiple collapse-tags endpoint="/admin/permissions/candidates" :params="{domain: 'directories'}" :model-value="modelValue[domain].directories || []" @update:model-value="change(domain, 'directories', $event)" /></el-form-item>
              <el-form-item v-if="domain !== 'records'" :label="domain === 'tasks' ? '指定三级任务（按固定编号授权）' : '指定指标／记录'"><el-select v-if="domain === 'core'" :aria-label="`${label}指定记录`" multiple clearable filterable collapse-tags :model-value="modelValue[domain].items || []" @update:model-value="change(domain, 'items', $event)"><el-option v-for="item in items(domain)" :key="item.id" :label="`${item.code || ''} ${item.name || `指标 ${item.id}`}${item.year ? `（${item.year}）` : ''}`" :value="item.id" /></el-select><RemoteCandidateSelect v-else :aria-label="`${label}指定记录`" multiple collapse-tags endpoint="/admin/permissions/candidates" :params="{domain: domain === 'expert' ? 'tasks' : domain, years: modelValue[domain].years || []}" :model-value="modelValue[domain].items || []" @update:model-value="change(domain, 'items', $event)" /></el-form-item>
            </el-form>
          </template>
          <p v-if="modelValue[domain].mode === 'BUSINESS'" class="muted">跟随当前责任人、审核人及部门负责人关系；不会自动分配新的业务职责。</p>
          <p v-if="domain === 'expert'" class="muted">仅限制评语范围；原有任务填报、审核和动态业务范围保留。配置前的旧专家继续沿用任务查阅范围。</p>
          <p v-if="domain === 'records'" class="muted">全校或部门范围仅包含已提交纪实。他人草稿仍不可查阅。</p>
          <p v-if="domain === 'budget'" class="muted">预算按整张月度表授权，不拆分部门导入权限。</p>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script setup>
import RemoteCandidateSelect from '../RemoteCandidateSelect.vue'
const props = defineProps({ modelValue: { type: Object, required: true }, options: { type: Object, default: () => ({}) }, personal: Boolean })
const emit = defineEmits(['update:modelValue'])
const modules = { tasks: '任务', performance: '绩效', achievements: '成果', core: '核心指标', records: '工作纪实', budget: '预算', expert: '专家评审' }
const roleModules = Object.fromEntries(Object.entries(modules).filter(([key]) => key !== 'expert'))
const modeNames = { ALL: '全校范围', NONE: '无数据', BUSINESS: '现有业务职责', OWN: '本人记录', CUSTOM: '组合选择', LEADERSHIP: '跟随领导查阅配置' }
const years = [2025, 2026, 2027, 2028, 2029]
const modes = domain => domain === 'budget' ? ['ALL', 'NONE'] : ['core', 'expert'].includes(domain) ? ['ALL', 'NONE', 'CUSTOM'] : ['ALL', 'NONE', 'BUSINESS', 'OWN', 'CUSTOM', 'LEADERSHIP']

const items = domain => (domain === 'tasks' ? (props.options.tasks || []).filter(t => Number(t.level) === 3) : props.options[domain] || []).filter(t => !t.year || !props.modelValue[domain]?.years?.length || props.modelValue[domain].years.includes(Number(t.year)))
const toggle = (domain, enabled) => { const next = { ...props.modelValue }; if (enabled) next[domain] = { mode: 'NONE' }; else delete next[domain]; emit('update:modelValue', next) }
const change = (domain, field, value) => { const next = { ...props.modelValue[domain], [field]: value }; if (field === 'mode') { delete next.departments; delete next.directories; delete next.items } emit('update:modelValue', { ...props.modelValue, [domain]: next }) }
</script>
<style scoped>
.muted{color:#778496;font-size:13px;line-height:1.7}.scope-fields{display:flex;gap:12px;margin:18px 0;flex-wrap:wrap}.scope-fields>.el-select{width:230px}.el-form-item .el-select{width:100%}.permission-scopes{border:1px solid #e4eaf2;border-radius:8px;padding:14px}
</style>
