import { useState } from 'react';
import {
  Building2, Network, Users, Shield, Bell, Settings,
  ChevronDown, ChevronRight, Search, Plus, MoreHorizontal,
  CheckCircle, XCircle, AlertTriangle,
  BarChart3, User, Sun, Moon, Home,
  TrendingUp, TrendingDown, Download, RefreshCw,
  Columns3, ChevronLeft, Filter, X,
  Trash2, Lock, Power, Palette, Globe, Type,
} from 'lucide-react';

type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'LOCKED' | 'MIGRATING';
type Density = 'compact' | 'standard' | 'comfortable';
type ColorScheme = 'blue' | 'violet' | 'emerald' | 'cyan';

interface Tenant {
  id: number; code: string; name: string; type: string; parent: string;
  level: number; status: TenantStatus; users: number; quota: number; quotaUsed: number; createdAt: string;
}

const mockTenants: Tenant[] = [
  { id: 1, code: 'GROUP_HQ', name: '集团总部', type: '集团', parent: '-', level: 1, status: 'ACTIVE', users: 1280, quota: 5000, quotaUsed: 1280, createdAt: '2025-01-15' },
  { id: 2, code: 'SUB_TECH', name: '科技公司', type: '子公司', parent: '集团总部', level: 2, status: 'ACTIVE', users: 320, quota: 1000, quotaUsed: 320, createdAt: '2025-02-20' },
  { id: 3, code: 'SUB_FIN', name: '财务公司', type: '子公司', parent: '集团总部', level: 2, status: 'ACTIVE', users: 156, quota: 800, quotaUsed: 156, createdAt: '2025-03-10' },
  { id: 4, code: 'PROJ_X1', name: '智慧园区项目', type: '项目', parent: '科技公司', level: 3, status: 'ACTIVE', users: 48, quota: 100, quotaUsed: 48, createdAt: '2025-04-05' },
  { id: 5, code: 'SUB_LOG', name: '物流公司', type: '子公司', parent: '集团总部', level: 2, status: 'SUSPENDED', users: 89, quota: 500, quotaUsed: 89, createdAt: '2025-03-22' },
  { id: 6, code: 'PROJ_Y2', name: '数据中台项目', type: '项目', parent: '科技公司', level: 3, status: 'MIGRATING', users: 12, quota: 50, quotaUsed: 12, createdAt: '2025-05-18' },
  { id: 7, code: 'EXT_PARTNER', name: '合作方A', type: '外部', parent: '集团总部', level: 2, status: 'LOCKED', users: 5, quota: 20, quotaUsed: 5, createdAt: '2025-06-01' },
  { id: 8, code: 'SUB_HR', name: '人力资源公司', type: '子公司', parent: '集团总部', level: 2, status: 'ACTIVE', users: 67, quota: 500, quotaUsed: 420, createdAt: '2025-06-15' },
  { id: 9, code: 'PROJ_Z3', name: '智能制造项目', type: '项目', parent: '科技公司', level: 3, status: 'ACTIVE', users: 34, quota: 100, quotaUsed: 78, createdAt: '2025-06-20' },
];

const statusLabel: Record<TenantStatus, string> = { ACTIVE: '正常', SUSPENDED: '已暂停', LOCKED: '已锁定', MIGRATING: '迁移中' };

function Sparkline({ data, color }: { data: number[]; color: string }) {
  const max = Math.max(...data), min = Math.min(...data), range = max - min || 1;
  const pts = data.map((v, i) => `${(i / (data.length - 1)) * 100},${28 - ((v - min) / range) * 22 - 3}`).join(' ');
  const areaPts = `0,28 ${pts} 100,28`;
  return (
    <svg width="100%" height="28" viewBox="0 0 100 28" preserveAspectRatio="none">
      <defs><linearGradient id={`grad-${color.replace('#', '')}`} x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor={color} stopOpacity="0.15" /><stop offset="100%" stopColor={color} stopOpacity="0" /></linearGradient></defs>
      <polygon points={areaPts} fill={`url(#grad-${color.replace('#', '')})`} />
      <polyline points={pts} fill="none" stroke={color} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

interface MenuItem { icon: typeof Building2; label: string; active?: boolean; badge?: string; }
interface MenuGroup { title: string; items: MenuItem[]; }

const menuGroups: MenuGroup[] = [
  { title: '租户与组织', items: [
    { icon: Building2, label: '租户管理', active: true, badge: '9' },
    { icon: Network, label: '组织架构' },
    { icon: Users, label: '用户管理' },
  ]},
  { title: '安全与权限', items: [{ icon: Shield, label: '权限管理' }]},
  { title: '运营', items: [
    { icon: BarChart3, label: '运营看板' },
    { icon: Bell, label: '消息中心', badge: '3' },
    { icon: Settings, label: '配置管理' },
  ]},
];

const quickFilters = [
  { label: '全部', count: 9, active: true },
  { label: '正常', count: 5 }, { label: '已暂停', count: 1 },
  { label: '已锁定', count: 1 }, { label: '迁移中', count: 1 },
];

const schemes: Record<ColorScheme, { name: string; light: Record<string, string>; dark: Record<string, string> }> = {
  blue: {
    name: '靛蓝',
    light: { primary: '#2563EB', primary600: '#1D4ED8', primary700: '#1E40AF', primary50: '#EFF6FF', primary100: '#DBEAFE' },
    dark: { primary: '#3B82F6', primary600: '#2563EB', primary700: '#1D4ED8', primary50: '#1E293B', primary100: '#334155' },
  },
  violet: {
    name: '紫罗兰',
    light: { primary: '#7C3AED', primary600: '#6D28D9', primary700: '#5B21B6', primary50: '#F5F3FF', primary100: '#EDE9FE' },
    dark: { primary: '#A78BFA', primary600: '#8B5CF6', primary700: '#7C3AED', primary50: '#2E1065', primary100: '#3B0764' },
  },
  emerald: {
    name: '翡翠',
    light: { primary: '#059669', primary600: '#047857', primary700: '#065F46', primary50: '#ECFDF5', primary100: '#D1FAE5' },
    dark: { primary: '#34D399', primary600: '#10B981', primary700: '#059669', primary50: '#052E16', primary100: '#064E3B' },
  },
  cyan: {
    name: '青碧',
    light: { primary: '#0891B2', primary600: '#0E7490', primary700: '#155E75', primary50: '#ECFEFF', primary100: '#CFFAFE' },
    dark: { primary: '#22D3EE', primary600: '#06B6D4', primary700: '#0891B2', primary50: '#083344', primary100: '#155E75' },
  },
};

const densityMap: Record<Density, Record<string, number>> = {
  compact: { rowH: 36, hdrH: 32, pad: 12, btnH: 28, btnSm: 24, cardP: 12, gap: 12, gapSm: 8, icon: 16, iconSm: 14, fs: 12, fsSm: 11, fsXs: 10, title: 18, statVal: 22, badgeH: 24, inputH: 28, menuH: 32, barH: 48, sideW: 200, radius: 8 },
  standard: { rowH: 44, hdrH: 38, pad: 16, btnH: 34, btnSm: 30, cardP: 16, gap: 16, gapSm: 12, icon: 18, iconSm: 15, fs: 13, fsSm: 12, fsXs: 11, title: 22, statVal: 26, badgeH: 30, inputH: 34, menuH: 36, barH: 56, sideW: 240, radius: 10 },
  comfortable: { rowH: 56, hdrH: 44, pad: 20, btnH: 40, btnSm: 36, cardP: 24, gap: 20, gapSm: 16, icon: 20, iconSm: 17, fs: 14, fsSm: 13, fsXs: 12, title: 26, statVal: 32, badgeH: 34, inputH: 40, menuH: 44, barH: 64, sideW: 260, radius: 12 },
};

export default function App() {
  const [collapsed, setCollapsed] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [expandedRow, setExpandedRow] = useState<number | null>(null);
  const [mode, setMode] = useState<'light' | 'dark'>('light');
  const [density, setDensity] = useState<Density>('standard');
  const [scheme, setScheme] = useState<ColorScheme>('blue');
  const [face, setFace] = useState<'admin' | 'portal'>('admin');
  const [openDropdown, setOpenDropdown] = useState<'theme' | 'size' | 'lang' | 'profile' | null>(null);

  const d = densityMap[density];
  const isDark = mode === 'dark';
  const sc = schemes[scheme][mode];
  const c = {
    primary: sc.primary, primary600: sc.primary600, primary700: sc.primary700,
    primary50: sc.primary50, primary100: sc.primary100,
    bg: isDark ? '#0F172A' : '#F8FAFC', card: isDark ? '#1E293B' : '#FFFFFF',
    text: isDark ? '#F1F5F9' : '#0F172A', textSec: isDark ? '#94A3B8' : '#475569',
    textMuted: isDark ? '#64748B' : '#94A3B8',
    border: isDark ? '#334155' : '#E2E8F0', borderLight: isDark ? '#1E293B' : '#F1F5F9',
    hover: isDark ? '#1E293B' : '#F8FAFC', inputBg: isDark ? '#0F172A' : '#FFFFFF',
    headerBg: isDark ? '#1E293B' : '#F8FAFC', codeBg: isDark ? '#334155' : '#F1F5F9',
    success: isDark ? '#4ADE80' : '#16A34A', successBg: isDark ? '#052E16' : '#F0FDF4',
    warning: isDark ? '#FBBF24' : '#D97706', warningBg: isDark ? '#422006' : '#FFFBEB',
    danger: isDark ? '#F87171' : '#DC2626', dangerBg: isDark ? '#450A0A' : '#FEF2F2',
    info: isDark ? '#22D3EE' : '#0891B2', infoBg: isDark ? '#083344' : '#ECFEFF',
    shadowHover: isDark ? '0 8px 24px rgba(0,0,0,0.4)' : '0 4px 12px rgba(0,0,0,0.08)',
  };
  const statusColor: Record<TenantStatus, string> = { ACTIVE: c.success, SUSPENDED: c.warning, LOCKED: c.danger, MIGRATING: c.info };
  const statusBg: Record<TenantStatus, string> = { ACTIVE: c.successBg, SUSPENDED: c.warningBg, LOCKED: c.dangerBg, MIGRATING: c.infoBg };

  const toggleSelect = (id: number) => { const n = new Set(selectedIds); n.has(id) ? n.delete(id) : n.add(id); setSelectedIds(n); };
  const allSelected = selectedIds.size === mockTenants.length;
  const toggleAll = () => setSelectedIds(allSelected ? new Set() : new Set(mockTenants.map(t => t.id)));

  const iconBtn = (onClick?: () => void, extra?: React.CSSProperties): React.CSSProperties => ({
    width: d.btnSm + 4, height: d.btnSm + 4, borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center',
    border: `1px solid ${c.border}`, backgroundColor: 'transparent', color: c.textSec, cursor: 'pointer', position: 'relative',
    transition: 'all 0.15s', ...extra,
  });
  const dropdownStyle: React.CSSProperties = {
    position: 'absolute', top: d.barH - 4, right: 0, borderRadius: 8, border: `1px solid ${c.border}`,
    backgroundColor: c.card, boxShadow: c.shadowHover, padding: 4, zIndex: 100, minWidth: 180,
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: c.bg, color: c.text, fontFamily: 'Inter, system-ui, sans-serif', display: 'flex' }}>
      {/* ========== Sidebar ========== */}
      <aside style={{ width: collapsed ? 64 : d.sideW, backgroundColor: c.card, borderRight: `1px solid ${c.border}`, display: 'flex', flexDirection: 'column', transition: 'width 0.2s', flexShrink: 0, position: 'sticky', top: 0, height: '100vh' }}>
        {/* Logo */}
        <div style={{ height: d.barH, display: 'flex', alignItems: 'center', gap: 10, padding: `0 ${d.gapSm}px`, borderBottom: `1px solid ${c.border}`, flexShrink: 0 }}>
          <div style={{ width: d.icon + 14, height: d.icon + 14, borderRadius: 8, background: `linear-gradient(135deg, ${c.primary}, ${c.primary600})`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, boxShadow: `0 2px 8px ${c.primary}40` }}>
            <span style={{ color: '#fff', fontWeight: 700, fontSize: d.fsSm }}>X</span>
          </div>
          {!collapsed && <div><div style={{ fontWeight: 700, fontSize: d.fs, lineHeight: 1 }}>XCMS</div><div style={{ fontSize: d.fsXs, color: c.textMuted, marginTop: 2 }}>集团中台</div></div>}
        </div>
        {/* Search */}
        {!collapsed && (
          <div style={{ padding: `${d.gapSm}px` }}>
            <div style={{ position: 'relative' }}>
              <Search size={d.iconSm} style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: c.textMuted }} />
              <input placeholder="搜索菜单..." style={{ height: d.inputH - 4, width: '100%', borderRadius: 6, border: `1px solid ${c.border}`, paddingLeft: d.icon + 14, paddingRight: 28, fontSize: d.fsSm, backgroundColor: c.headerBg, color: c.text, outline: 'none' }} />
              <kbd style={{ position: 'absolute', right: 8, top: '50%', transform: 'translateY(-50%)', fontSize: d.fsXs, color: c.textMuted, backgroundColor: c.card, border: `1px solid ${c.border}`, borderRadius: 3, padding: '1px 4px' }}>⌘K</kbd>
            </div>
          </div>
        )}
        {/* Menu */}
        <nav style={{ flex: 1, overflowY: 'auto', padding: `${d.gapSm}px 0` }}>
          {menuGroups.map((group, gi) => (
            <div key={gi} style={{ marginBottom: 4 }}>
              {!collapsed && <div style={{ padding: `${d.gapSm}px ${d.gap}px ${d.pad - 4}px`, fontSize: d.fsXs, fontWeight: 600, color: c.textMuted, textTransform: 'uppercase', letterSpacing: '0.05em' }}>{group.title}</div>}
              {group.items.map((item, i) => (
                <button key={i} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: d.gapSm, padding: `0 ${d.gap}px`, height: d.menuH, fontSize: d.fs, cursor: 'pointer', transition: 'all 0.15s', backgroundColor: item.active ? c.primary50 : 'transparent', color: item.active ? c.primary : c.textSec, fontWeight: item.active ? 500 : 400, border: 'none', borderLeft: item.active ? `2px solid ${c.primary}` : '2px solid transparent' }}>
                  <item.icon size={d.iconSm} style={{ flexShrink: 0 }} />
                  {!collapsed && <span style={{ flex: 1, textAlign: 'left' }}>{item.label}</span>}
                  {!collapsed && item.badge && <span style={{ fontSize: d.fsXs, fontWeight: 600, padding: `1px ${d.pad - 4}px`, borderRadius: 10, backgroundColor: item.active ? c.primary : c.codeBg, color: item.active ? '#fff' : c.textMuted }}>{item.badge}</span>}
                </button>
              ))}
            </div>
          ))}
        </nav>
        {/* User */}
        <div style={{ borderTop: `1px solid ${c.border}`, padding: d.gapSm, flexShrink: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm, padding: '4px', borderRadius: 6, cursor: 'pointer' }}>
            <div style={{ width: d.icon + 14, height: d.icon + 14, borderRadius: '50%', backgroundColor: c.primary100, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, border: `2px solid ${c.card}` }}><User size={d.iconSm} style={{ color: c.primary700 }} /></div>
            {!collapsed && <div style={{ flex: 1, minWidth: 0 }}><div style={{ fontSize: d.fs, fontWeight: 500, color: c.text, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>超级管理员</div><div style={{ fontSize: d.fsXs, color: c.textMuted }}>集团总部 · L0</div></div>}
          </div>
        </div>
        <button onClick={() => setCollapsed(!collapsed)} style={{ height: d.menuH, display: 'flex', alignItems: 'center', justifyContent: 'center', borderTop: `1px solid ${c.border}`, color: c.textMuted, cursor: 'pointer', backgroundColor: 'transparent', border: 'none', borderTopWidth: 1, borderTopStyle: 'solid', borderTopColor: c.border }}><ChevronLeft size={d.iconSm} style={{ transform: collapsed ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} /></button>
      </aside>

      {/* ========== Main ========== */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* TopBar */}
        <header style={{ height: d.barH, backgroundColor: c.card, borderBottom: `1px solid ${c.border}`, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: `0 ${d.gap}px`, flexShrink: 0, position: 'sticky', top: 0, zIndex: 20 }}>
          {/* 左上：管理面/业务面切换 + 面包屑 */}
          <div style={{ display: 'flex', alignItems: 'center', gap: d.gap }}>
            <div style={{ display: 'flex', borderRadius: 6, padding: 2, backgroundColor: c.headerBg }}>
              <button onClick={() => setFace('admin')} style={{ padding: `4px ${d.pad}px`, fontSize: d.fsXs, fontWeight: 500, borderRadius: 4, color: face === 'admin' ? '#fff' : c.textMuted, backgroundColor: face === 'admin' ? c.primary : 'transparent', border: 'none', cursor: 'pointer', transition: 'all 0.15s' }}>管理面</button>
              <button onClick={() => setFace('portal')} style={{ padding: `4px ${d.pad}px`, fontSize: d.fsXs, fontWeight: 500, borderRadius: 4, color: face === 'portal' ? '#fff' : c.textMuted, backgroundColor: face === 'portal' ? c.primary : 'transparent', border: 'none', cursor: 'pointer', transition: 'all 0.15s' }}>业务面</button>
            </div>
            <div style={{ width: 1, height: d.icon, backgroundColor: c.border }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: d.fsSm }}>
              <Home size={d.iconSm} style={{ color: c.textMuted }} /><span style={{ color: c.textMuted }}>首页</span><ChevronRight size={d.iconSm - 2} style={{ color: c.textMuted }} /><span style={{ color: c.text, fontWeight: 500 }}>{face === 'admin' ? '租户管理' : '工作台'}</span>
            </div>
          </div>

          {/* 右上：主题切换 → Size切换 → 语言 → 通知 → 用户 */}
          <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm }}>
            {/* 主题色切换 */}
            <div style={{ position: 'relative' }}>
              <button onClick={() => setOpenDropdown(openDropdown === 'theme' ? null : 'theme')} style={iconBtn()} title="主题色">
                <Palette size={d.iconSm} />
                <span style={{ position: 'absolute', bottom: 4, right: 4, width: 8, height: 8, borderRadius: '50%', backgroundColor: c.primary, border: `1.5px solid ${c.card}` }} />
              </button>
              {openDropdown === 'theme' && (
                <div style={dropdownStyle}>
                  <div style={{ padding: `4px 8px`, fontSize: d.fsXs, color: c.textMuted, fontWeight: 600 }}>主题色</div>
                  {(Object.keys(schemes) as ColorScheme[]).map((key) => (
                    <button key={key} onClick={() => { setScheme(key); setOpenDropdown(null); }} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, transition: 'background 0.15s', backgroundColor: scheme === key ? c.primary50 : 'transparent', color: scheme === key ? c.primary : c.text }} onMouseEnter={(e) => { if (scheme !== key) e.currentTarget.style.backgroundColor = c.hover; }} onMouseLeave={(e) => { if (scheme !== key) e.currentTarget.style.backgroundColor = 'transparent'; }}>
                      <span style={{ width: 14, height: 14, borderRadius: '50%', backgroundColor: schemes[key][mode].primary, border: `2px solid ${scheme === key ? c.primary : 'transparent'}` }} />
                      <span style={{ flex: 1, textAlign: 'left' }}>{schemes[key].name}</span>
                      {scheme === key && <CheckCircle size={d.iconSm - 2} style={{ color: c.primary }} />}
                    </button>
                  ))}
                  <div style={{ height: 1, backgroundColor: c.border, margin: '4px 0' }} />
                  <button onClick={() => { setMode(mode === 'light' ? 'dark' : 'light'); }} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, backgroundColor: 'transparent', color: c.text }}>
                    {mode === 'light' ? <Moon size={d.iconSm - 2} /> : <Sun size={d.iconSm - 2} />}
                    <span style={{ flex: 1, textAlign: 'left' }}>{mode === 'light' ? '暗色模式' : '亮色模式'}</span>
                  </button>
                </div>
              )}
            </div>

            {/* Size 切换 */}
            <div style={{ position: 'relative' }}>
              <button onClick={() => setOpenDropdown(openDropdown === 'size' ? null : 'size')} style={iconBtn()} title="密度">
                <Type size={d.iconSm} />
              </button>
              {openDropdown === 'size' && (
                <div style={dropdownStyle}>
                  <div style={{ padding: `4px 8px`, fontSize: d.fsXs, color: c.textMuted, fontWeight: 600 }}>界面密度</div>
                  {(['compact', 'standard', 'comfortable'] as Density[]).map((dn) => (
                    <button key={dn} onClick={() => { setDensity(dn); setOpenDropdown(null); }} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, transition: 'background 0.15s', backgroundColor: density === dn ? c.primary50 : 'transparent', color: density === dn ? c.primary : c.text }} onMouseEnter={(e) => { if (density !== dn) e.currentTarget.style.backgroundColor = c.hover; }} onMouseLeave={(e) => { if (density !== dn) e.currentTarget.style.backgroundColor = 'transparent'; }}>
                      <span style={{ flex: 1, textAlign: 'left' }}>{dn === 'compact' ? '紧凑' : dn === 'standard' ? '标准' : '宽松'}</span>
                      {density === dn && <CheckCircle size={d.iconSm - 2} style={{ color: c.primary }} />}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* 语言 */}
            <div style={{ position: 'relative' }}>
              <button onClick={() => setOpenDropdown(openDropdown === 'lang' ? null : 'lang')} style={iconBtn()} title="语言">
                <Globe size={d.iconSm} />
              </button>
              {openDropdown === 'lang' && (
                <div style={dropdownStyle}>
                  <div style={{ padding: `4px 8px`, fontSize: d.fsXs, color: c.textMuted, fontWeight: 600 }}>语言</div>
                  {[{ label: '简体中文', active: true }, { label: 'English', active: false }].map((lang) => (
                    <button key={lang.label} onClick={() => setOpenDropdown(null)} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, backgroundColor: lang.active ? c.primary50 : 'transparent', color: lang.active ? c.primary : c.text }}>
                      <span style={{ flex: 1, textAlign: 'left' }}>{lang.label}</span>
                      {lang.active && <CheckCircle size={d.iconSm - 2} style={{ color: c.primary }} />}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* 通知 */}
            <button style={iconBtn(undefined, { position: 'relative' })} title="通知">
              <Bell size={d.iconSm} />
              <span style={{ position: 'absolute', top: 5, right: 5, width: 7, height: 7, borderRadius: '50%', backgroundColor: c.danger, border: `1.5px solid ${c.card}` }} />
            </button>

            {/* 用户 Profile */}
            <div style={{ position: 'relative' }}>
              <button onClick={() => setOpenDropdown(openDropdown === 'profile' ? null : 'profile')} style={{ display: 'flex', alignItems: 'center', gap: d.gapSm - 4, padding: '2px 4px 2px 2px', borderRadius: 20, border: `1px solid ${c.border}`, backgroundColor: 'transparent', cursor: 'pointer', transition: 'all 0.15s' }} onMouseEnter={(e) => e.currentTarget.style.borderColor = c.primary} onMouseLeave={(e) => e.currentTarget.style.borderColor = c.border}>
                <div style={{ width: d.icon + 10, height: d.icon + 10, borderRadius: '50%', backgroundColor: c.primary100, display: 'flex', alignItems: 'center', justifyContent: 'center' }}><User size={d.iconSm} style={{ color: c.primary700 }} /></div>
                <span style={{ fontSize: d.fsSm, fontWeight: 500, color: c.text }}>管理员</span>
                <ChevronDown size={d.iconSm - 2} style={{ color: c.textMuted }} />
              </button>
              {openDropdown === 'profile' && (
                <div style={dropdownStyle}>
                  <div style={{ padding: `${d.gapSm}px ${d.pad}px`, borderBottom: `1px solid ${c.borderLight}`, display: 'flex', alignItems: 'center', gap: d.gapSm }}>
                    <div style={{ width: d.icon + 12, height: d.icon + 12, borderRadius: '50%', backgroundColor: c.primary100, display: 'flex', alignItems: 'center', justifyContent: 'center' }}><User size={d.iconSm} style={{ color: c.primary700 }} /></div>
                    <div><div style={{ fontSize: d.fs, fontWeight: 600, color: c.text }}>超级管理员</div><div style={{ fontSize: d.fsXs, color: c.textMuted }}>集团总部 · L0</div></div>
                  </div>
                  <div style={{ padding: 4 }}>
                    {[{ icon: User, label: '个人中心' }, { icon: Settings, label: '偏好设置' }].map((item) => (
                      <button key={item.label} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, backgroundColor: 'transparent', color: c.text }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = c.hover} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                        <item.icon size={d.iconSm - 2} /><span style={{ flex: 1, textAlign: 'left' }}>{item.label}</span>
                      </button>
                    ))}
                    <div style={{ height: 1, backgroundColor: c.border, margin: '4px 0' }} />
                    <button style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 8, padding: '6px 8px', borderRadius: 5, border: 'none', cursor: 'pointer', fontSize: d.fsSm, backgroundColor: 'transparent', color: c.danger }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = c.dangerBg} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>
                      <Power size={d.iconSm - 2} /><span style={{ flex: 1, textAlign: 'left' }}>退出登录</span>
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </header>

        {/* Content */}
        <main style={{ flex: 1, overflow: 'auto', padding: d.gap, maxWidth: 1600, width: '100%', margin: '0 auto' }}>
          {/* Page Header */}
          <div style={{ marginBottom: d.gap, display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
            <div>
              <h1 style={{ fontSize: d.title, fontWeight: 700, color: c.text, margin: 0, lineHeight: 1.2 }}>租户管理</h1>
              <p style={{ fontSize: d.fsSm, color: c.textSec, margin: '4px 0 0 0' }}>管理集团下属所有租户、子公司及项目</p>
            </div>
            <div style={{ display: 'flex', gap: d.gapSm }}>
              <button style={{ height: d.btnH, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 6, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6, transition: 'all 0.15s' }} onMouseEnter={(e) => { e.currentTarget.style.borderColor = c.primary; e.currentTarget.style.color = c.primary; }} onMouseLeave={(e) => { e.currentTarget.style.borderColor = c.border; e.currentTarget.style.color = c.textSec; }}><Download size={d.iconSm} /> 导出</button>
              <button style={{ height: d.btnH, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 6, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}><RefreshCw size={d.iconSm} /> 刷新</button>
              <button style={{ height: d.btnH, padding: `0 ${d.gap}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 6, border: 'none', cursor: 'pointer', backgroundColor: c.primary, color: '#fff', display: 'flex', alignItems: 'center', gap: 6, boxShadow: `0 2px 8px ${c.primary}30` }}><Plus size={d.iconSm} /> 新建租户</button>
            </div>
          </div>

          {/* Stats */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: d.gap, marginBottom: d.gap }}>
            {[
              { label: '租户总数', value: '9', change: '+2', trend: 'up', spark: [3, 4, 4, 5, 5, 6, 6, 7, 8, 9], color: c.primary, icon: Building2 },
              { label: '活跃用户', value: '2,011', change: '+12%', trend: 'up', spark: [1200, 1350, 1400, 1500, 1650, 1750, 1850, 1910, 1980, 2011], color: c.success, icon: Users },
              { label: '配额预警', value: '1', change: '+1', trend: 'up', spark: [0, 0, 1, 0, 1, 1, 0, 1, 1, 1], color: c.warning, icon: AlertTriangle },
              { label: '异常告警', value: '2', change: '-3', trend: 'down', spark: [8, 7, 6, 5, 5, 4, 3, 2, 2, 2], color: c.danger, icon: XCircle },
            ].map((stat, i) => (
              <div key={i} style={{ backgroundColor: c.card, borderRadius: d.radius, border: `1px solid ${c.border}`, padding: d.cardP, transition: 'box-shadow 0.2s', position: 'relative', overflow: 'hidden' }} onMouseEnter={(e) => e.currentTarget.style.boxShadow = c.shadowHover} onMouseLeave={(e) => e.currentTarget.style.boxShadow = 'none'}>
                <div style={{ position: 'absolute', left: 0, top: 0, bottom: 0, width: 3, backgroundColor: stat.color }} />
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: d.gapSm - 4 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm }}>
                    <div style={{ width: d.icon + 16, height: d.icon + 16, borderRadius: d.radius - 2, backgroundColor: stat.color + '15', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><stat.icon size={d.icon} style={{ color: stat.color }} /></div>
                    <span style={{ fontSize: d.fsSm, color: c.textSec }}>{stat.label}</span>
                  </div>
                  <span style={{ fontSize: d.fsXs, fontWeight: 600, display: 'flex', alignItems: 'center', gap: 2, color: stat.trend === 'up' ? (stat.label === '配额预警' ? c.danger : c.success) : c.success }}>{stat.trend === 'up' ? <TrendingUp size={d.fsXs} /> : <TrendingDown size={d.fsXs} />}{stat.change}</span>
                </div>
                <div style={{ fontSize: d.statVal, fontWeight: 700, color: c.text, marginBottom: 2 }}>{stat.value}</div>
                <div style={{ marginTop: 4 }}><Sparkline data={stat.spark} color={stat.color} /></div>
              </div>
            ))}
          </div>

          {/* Table */}
          <div style={{ backgroundColor: c.card, borderRadius: d.radius, border: `1px solid ${c.border}`, overflow: 'hidden' }}>
            {/* Filter */}
            <div style={{ padding: `${d.gapSm}px ${d.gap}px`, borderBottom: `1px solid ${c.borderLight}`, display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: d.gap }}>
              <div style={{ display: 'flex', gap: d.gapSm - 4 }}>
                {quickFilters.map((f) => (
                  <button key={f.label} style={{ height: d.badgeH, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 15, cursor: 'pointer', transition: 'all 0.15s', border: 'none', backgroundColor: f.active ? c.primary : c.codeBg, color: f.active ? '#fff' : c.textSec, display: 'flex', alignItems: 'center', gap: 4 }}>{f.label}<span style={{ fontSize: d.fsXs, opacity: 0.7 }}>{f.count}</span></button>
                ))}
              </div>
              <div style={{ display: 'flex', gap: d.gapSm }}>
                <div style={{ position: 'relative', width: 240 }}>
                  <Search size={d.fs} style={{ position: 'absolute', left: 10, top: '50%', transform: 'translateY(-50%)', color: c.textMuted }} />
                  <input placeholder="搜索名称/编码..." style={{ height: d.inputH, width: '100%', borderRadius: 6, border: `1px solid ${c.border}`, paddingLeft: 32, paddingRight: 8, fontSize: d.fsSm, backgroundColor: c.inputBg, color: c.text, outline: 'none', transition: 'border 0.15s' }} onFocus={(e) => e.currentTarget.style.borderColor = c.primary} onBlur={(e) => e.currentTarget.style.borderColor = c.border} />
                </div>
                <button style={{ width: d.inputH, height: d.inputH, borderRadius: 6, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Filter size={d.fs} /></button>
                <button style={{ width: d.inputH, height: d.inputH, borderRadius: 6, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Columns3 size={d.fs} /></button>
              </div>
            </div>

            {/* Batch Bar */}
            {selectedIds.size > 0 && (
              <div style={{ padding: `${d.gapSm}px ${d.gap}px`, backgroundColor: c.primary50, borderBottom: `1px solid ${c.borderLight}`, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm }}>
                  <span style={{ fontSize: d.fsSm, fontWeight: 500, color: c.primary }}>已选择 <span style={{ fontWeight: 700 }}>{selectedIds.size}</span> 项</span>
                  <button onClick={() => setSelectedIds(new Set())} style={{ fontSize: d.fsXs, color: c.textMuted, border: 'none', background: 'transparent', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 2 }}><X size={d.fsXs} /> 清除</button>
                </div>
                <div style={{ display: 'flex', gap: d.gapSm }}>
                  <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsXs, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}><Lock size={d.fsXs} /> 批量锁定</button>
                  <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsXs, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}><Power size={d.fsXs} /> 批量停用</button>
                  <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsXs, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.danger}40`, backgroundColor: 'transparent', color: c.danger, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}><Trash2 size={d.fsXs} /> 批量删除</button>
                </div>
              </div>
            )}

            {/* Table Body */}
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', fontSize: d.fs, borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ backgroundColor: c.headerBg, borderBottom: `1px solid ${c.border}` }}>
                    <th style={{ width: d.icon + 24, padding: `0 ${d.pad - 4}px`, height: d.hdrH, textAlign: 'center' }}>
                      <input type="checkbox" checked={allSelected} onChange={toggleAll} style={{ cursor: 'pointer', accentColor: c.primary, width: d.icon - 2, height: d.icon - 2 }} />
                    </th>
                    {['租户名称', '编码', '类型', '上级', '层级', '用户数', '配额使用', '状态', '创建时间', ''].map((h, idx) => (
                      <th key={idx} style={{ textAlign: [5, 6, 7].includes(idx) ? 'center' : idx === 9 ? 'right' : 'left', padding: `0 ${d.pad}px`, height: d.hdrH, fontWeight: 500, color: c.textSec, fontSize: d.fsSm, whiteSpace: 'nowrap' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {mockTenants.map((t) => {
                    const isSel = selectedIds.has(t.id);
                    const isExp = expandedRow === t.id;
                    const quotaPct = Math.round((t.quotaUsed / t.quota) * 100);
                    return (
                      <div key={t.id} style={{ display: 'contents' }}>
                        <tr style={{ height: d.rowH, cursor: 'pointer', transition: 'background 0.1s', backgroundColor: isSel ? c.primary50 : 'transparent', borderBottom: `1px solid ${c.borderLight}` }} onMouseEnter={(e) => { if (!isSel) e.currentTarget.style.backgroundColor = c.hover; }} onMouseLeave={(e) => { if (!isSel) e.currentTarget.style.backgroundColor = 'transparent'; }}>
                          <td style={{ padding: `0 ${d.pad - 4}px`, textAlign: 'center' }}>
                            <input type="checkbox" checked={isSel} onChange={() => toggleSelect(t.id)} style={{ cursor: 'pointer', accentColor: c.primary, width: d.icon - 2, height: d.icon - 2 }} />
                          </td>
                          <td style={{ padding: `0 ${d.pad}px` }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm - 4 }} onClick={() => { setExpandedRow(isExp ? null : t.id); }}>
                              <ChevronRight size={d.iconSm - 1} style={{ color: c.textMuted, transform: isExp ? 'rotate(90deg)' : 'none', transition: 'transform 0.15s', flexShrink: 0 }} />
                              <div style={{ width: d.icon + 8, height: d.icon + 8, borderRadius: 6, backgroundColor: statusBg[t.status] + '80', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Building2 size={d.icon - 2} style={{ color: statusColor[t.status] }} /></div>
                              <span style={{ fontWeight: 500, color: c.text }}>{t.name}</span>
                            </div>
                          </td>
                          <td style={{ padding: `0 ${d.pad}px` }}><code style={{ fontSize: d.fsXs, fontFamily: 'monospace', padding: `2px ${d.pad - 6}px`, borderRadius: 3, backgroundColor: c.codeBg, color: c.textSec }}>{t.code}</code></td>
                          <td style={{ padding: `0 ${d.pad}px`, color: c.textSec }}>{t.type}</td>
                          <td style={{ padding: `0 ${d.pad}px`, color: c.textMuted }}>{t.parent}</td>
                          <td style={{ padding: `0 ${d.pad}px`, textAlign: 'center' }}><span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: d.icon + 4, height: d.icon + 4, borderRadius: 5, backgroundColor: c.codeBg, color: c.textSec, fontSize: d.fsXs, fontWeight: 600 }}>{t.level}</span></td>
                          <td style={{ padding: `0 ${d.pad}px`, textAlign: 'center', color: c.textSec }}>{t.users.toLocaleString()}</td>
                          <td style={{ padding: `0 ${d.pad}px` }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: d.gapSm - 4, minWidth: 120 }}>
                              <div style={{ flex: 1, height: 6, borderRadius: 3, backgroundColor: c.codeBg, overflow: 'hidden' }}><div style={{ width: `${quotaPct}%`, height: '100%', borderRadius: 3, backgroundColor: quotaPct > 80 ? c.danger : quotaPct > 60 ? c.warning : c.success, transition: 'width 0.3s' }} /></div>
                              <span style={{ fontSize: d.fsXs, color: c.textMuted, minWidth: 36, fontWeight: 500 }}>{quotaPct}%</span>
                            </div>
                          </td>
                          <td style={{ padding: `0 ${d.pad}px`, textAlign: 'center' }}>
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5, padding: `2px ${d.pad - 4}px`, borderRadius: 12, fontSize: d.fsXs, fontWeight: 500, backgroundColor: statusBg[t.status], color: statusColor[t.status] }}>
                              <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: statusColor[t.status] }} />{statusLabel[t.status]}
                            </span>
                          </td>
                          <td style={{ padding: `0 ${d.pad}px`, color: c.textMuted, fontSize: d.fsXs, whiteSpace: 'nowrap' }}>{t.createdAt}</td>
                          <td style={{ padding: `0 ${d.pad}px`, textAlign: 'right' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 2 }}>
                              <button style={{ fontSize: d.fsSm, fontWeight: 500, color: c.primary, border: 'none', background: 'transparent', cursor: 'pointer', padding: `4px ${d.pad - 4}px`, borderRadius: 4, transition: 'background 0.15s' }} onMouseEnter={(e) => e.currentTarget.style.backgroundColor = c.primary50} onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}>详情</button>
                              <button style={{ width: d.icon + 12, height: d.icon + 12, borderRadius: 5, display: 'flex', alignItems: 'center', justifyContent: 'center', color: c.textMuted, border: 'none', background: 'transparent', cursor: 'pointer', transition: 'all 0.15s' }} onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = c.codeBg; e.currentTarget.style.color = c.text; }} onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = c.textMuted; }}><MoreHorizontal size={d.iconSm - 1} /></button>
                            </div>
                          </td>
                        </tr>
                        {isExp && (
                          <tr>
                            <td colSpan={11} style={{ padding: `${d.cardP}px ${d.gap}px ${d.cardP}px ${d.icon + 24 + d.pad + 16}px`, backgroundColor: c.hover, borderBottom: `1px solid ${c.borderLight}` }}>
                              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: d.gap }}>
                                {[{ label: '租户编码', value: t.code }, { label: '租户类型', value: t.type }, { label: '上级租户', value: t.parent }, { label: '层级', value: `第 ${t.level} 级` }, { label: '状态', value: statusLabel[t.status] }, { label: '用户总数', value: `${t.users.toLocaleString()} 人` }, { label: '配额上限', value: `${t.quota.toLocaleString()}` }, { label: '已用配额', value: `${t.quotaUsed.toLocaleString()} (${quotaPct}%)` }, { label: '剩余配额', value: `${(t.quota - t.quotaUsed).toLocaleString()}` }, { label: '创建时间', value: t.createdAt }].map((dt, di) => (
                                  <div key={di}><div style={{ fontSize: d.fsXs, color: c.textMuted, marginBottom: 2 }}>{dt.label}</div><div style={{ fontSize: d.fs, fontWeight: 500, color: c.text }}>{dt.value}</div></div>
                                ))}
                              </div>
                              <div style={{ display: 'flex', gap: d.gapSm, marginTop: d.gap }}>
                                <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer' }}>编辑</button>
                                <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer' }}>配额管理</button>
                                <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer' }}>功能开关</button>
                                <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.border}`, backgroundColor: c.card, color: c.textSec, cursor: 'pointer' }}>迁移</button>
                                <div style={{ flex: 1 }} />
                                <button style={{ height: d.btnSm, padding: `0 ${d.pad}px`, fontSize: d.fsSm, fontWeight: 500, borderRadius: 5, border: `1px solid ${c.danger}40`, backgroundColor: 'transparent', color: c.danger, cursor: 'pointer' }}>停用</button>
                              </div>
                            </td>
                          </tr>
                        )}
                      </div>
                    );
                  })}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            <div style={{ padding: `${d.gapSm}px ${d.gap}px`, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderTop: `1px solid ${c.borderLight}` }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: d.gap }}>
                <span style={{ fontSize: d.fsXs, color: c.textMuted }}>共 <span style={{ fontWeight: 600, color: c.text }}>9</span> 条</span>
                <div style={{ width: 1, height: d.icon, backgroundColor: c.border }} />
                <select style={{ height: d.badgeH, borderRadius: 5, border: `1px solid ${c.border}`, fontSize: d.fsXs, backgroundColor: c.inputBg, color: c.textSec, outline: 'none', padding: '0 8px', cursor: 'pointer' }}><option>10 条/页</option><option>20 条/页</option><option>50 条/页</option></select>
              </div>
              <div style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
                <button style={{ width: d.badgeH, height: d.badgeH, borderRadius: 5, display: 'flex', alignItems: 'center', justifyContent: 'center', border: `1px solid ${c.border}`, backgroundColor: 'transparent', color: c.textMuted, cursor: 'pointer' }}><ChevronLeft size={d.iconSm - 1} /></button>
                <button style={{ minWidth: d.badgeH, height: d.badgeH, padding: '0 8px', borderRadius: 5, fontSize: d.fsXs, fontWeight: 600, border: 'none', backgroundColor: c.primary, color: '#fff', cursor: 'pointer' }}>1</button>
                <span style={{ fontSize: d.fsXs, color: c.textMuted, padding: '0 4px' }}>/ 1</span>
                <button style={{ width: d.badgeH, height: d.badgeH, borderRadius: 5, display: 'flex', alignItems: 'center', justifyContent: 'center', border: `1px solid ${c.border}`, backgroundColor: 'transparent', color: c.textSec, cursor: 'pointer' }}><ChevronLeft size={d.iconSm - 1} style={{ transform: 'rotate(180deg)' }} /></button>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
