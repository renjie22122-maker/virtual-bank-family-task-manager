import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, dateTime, money } from './api'

const STATUS = {
  All: 'All tasks', WaitforCheck: 'Pending approval', Reject: 'Rejected', ToDo: 'To do',
  Doing: 'In progress', WaitforConfirm: 'Awaiting confirmation', Done: 'Completed', OverDue: 'Overdue',
}

const Icon = ({ name }) => <span className="icon" aria-hidden="true">{{
  home: '⌂', tasks: '✓', wallet: '◇', family: '♧', logout: '↗', plus: '+', arrow: '→', close: '×',
}[name]}</span>

function App() {
  const [token, setToken] = useState(() => localStorage.getItem('familyflow-token'))
  const [view, setView] = useState('overview')
  const [dashboard, setDashboard] = useState(null)
  const [accounts, setAccounts] = useState([])
  const [tasks, setTasks] = useState([])
  const [status, setStatus] = useState('All')
  const [sort, setSort] = useState('deadline')
  const [modal, setModal] = useState(null)
  const [notice, setNotice] = useState(null)
  const [loading, setLoading] = useState(false)

  const load = useCallback(async () => {
    if (!token) return
    setLoading(true)
    try {
      const [summary, accountList, taskList] = await Promise.all([
        api('/api/dashboard'), api('/api/accounts'), api(`/api/tasks?status=${status}&sort=${sort}`),
      ])
      setDashboard(summary); setAccounts(accountList); setTasks(taskList)
    } catch (error) {
      setNotice({ type: 'error', text: error.message })
      if (!localStorage.getItem('familyflow-token')) setToken(null)
    } finally { setLoading(false) }
  }, [token, status, sort])

  useEffect(() => { load() }, [load])

  const onAuth = result => {
    localStorage.setItem('familyflow-token', result.token); setToken(result.token); setDashboard(null)
  }
  const logout = async () => {
    await api('/api/auth/logout', { method: 'POST' }).catch(() => {})
    localStorage.removeItem('familyflow-token'); setToken(null); setDashboard(null)
  }
  const mutate = async (operation, success) => {
    try { await operation(); setModal(null); setNotice({ type: 'success', text: success }); await load() }
    catch (error) { setNotice({ type: 'error', text: error.message }) }
  }

  if (!token) return <AuthScreen onAuth={onAuth} />
  if (!dashboard) return <div className="splash"><div className="brand-mark">F</div><span>Loading your family space…</span></div>

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">F</div><div><strong>FamilyFlow</strong><small>Money meets momentum</small></div></div>
      <nav>
        <Nav active={view === 'overview'} icon="home" label="Overview" onClick={() => setView('overview')} />
        <Nav active={view === 'tasks'} icon="tasks" label="Tasks" count={dashboard.openTasks} onClick={() => setView('tasks')} />
        <Nav active={view === 'accounts'} icon="wallet" label="Accounts" onClick={() => setView('accounts')} />
        <Nav active={view === 'family'} icon="family" label="Family" onClick={() => setView('family')} />
      </nav>
      <div className="sidebar-profile"><Avatar name={dashboard.user.userName} /><div><strong>{dashboard.user.userName}</strong><small>{dashboard.user.userType}</small></div><button onClick={logout} title="Sign out"><Icon name="logout" /></button></div>
    </aside>
    <main>
      <header className="topbar"><div><p className="eyebrow">Family workspace</p><h1>{titleFor(view)}</h1></div><div className="header-actions"><button className="ghost" onClick={load}>Refresh</button><button className="primary" onClick={() => setModal('task')}><Icon name="plus" /> New task</button></div></header>
      {notice && <div className={`notice ${notice.type}`}><span>{notice.text}</span><button onClick={() => setNotice(null)}>×</button></div>}
      {loading && <div className="progress" />}
      {view === 'overview' && <Overview dashboard={dashboard} accounts={accounts} tasks={tasks} setView={setView} setModal={setModal} />}
      {view === 'tasks' && <TasksPage tasks={tasks} user={dashboard.user} status={status} setStatus={setStatus} sort={sort} setSort={setSort} mutate={mutate} setModal={setModal} />}
      {view === 'accounts' && <AccountsPage accounts={accounts} setModal={setModal} />}
      {view === 'family' && <FamilyPage dashboard={dashboard} />}
    </main>
    {modal === 'task' && <TaskModal members={dashboard.familyMembers} onClose={() => setModal(null)} onSave={data => mutate(() => api('/api/tasks', { method: 'POST', body: JSON.stringify(data) }), 'Task created.')} />}
    {modal === 'account' && <AccountModal onClose={() => setModal(null)} onSave={data => mutate(() => api('/api/accounts', { method: 'POST', body: JSON.stringify(data) }), 'Account created.')} />}
    {modal?.type === 'money' && <MoneyModal data={modal} onClose={() => setModal(null)} onSave={(path, data) => mutate(() => api(path, { method: 'POST', body: JSON.stringify(data) }), 'Account updated.')} />}
    {modal?.type === 'confirm' && <ConfirmModal task={modal.task} onClose={() => setModal(null)} onSave={bonus => mutate(() => api(`/api/tasks/${modal.task.taskId}/confirm`, { method: 'POST', body: JSON.stringify({ bonus }) }), 'Task confirmed and reward paid.')} />}
  </div>
}

function AuthScreen({ onAuth }) {
  const [mode, setMode] = useState('login'); const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ userName: '', password: '', userType: 'parent', familyGroupId: '' })
  const submit = async event => {
    event.preventDefault(); setBusy(true); setError('')
    try { onAuth(await api(`/api/auth/${mode}`, { method: 'POST', body: JSON.stringify(form) })) }
    catch (e) { setError(e.message) } finally { setBusy(false) }
  }
  return <div className="auth-page">
    <section className="auth-story"><div className="brand light"><div className="brand-mark">F</div><strong>FamilyFlow</strong></div><div className="story-copy"><p className="eyebrow">A calmer way to grow together</p><h1>Turn everyday tasks into lifelong money skills.</h1><p>One shared space for responsibilities, rewards and better family conversations.</p><div className="story-stats"><span><strong>Simple</strong>Clear task progress</span><span><strong>Safe</strong>Virtual family banking</span></div></div><div className="orb orb-one"/><div className="orb orb-two"/></section>
    <section className="auth-panel"><div className="auth-card"><p className="eyebrow">Welcome to FamilyFlow</p><h2>{mode === 'login' ? 'Sign in to your space' : 'Create your family profile'}</h2><p className="subtle">{mode === 'login' ? 'Pick up where your family left off.' : 'Parents create a group; children join with its ID.'}</p>
      <div className="segmented"><button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Sign in</button><button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Register</button></div>
      <form onSubmit={submit}><Field label="Username"><input required minLength="2" value={form.userName} onChange={e => setForm({...form, userName: e.target.value})} placeholder="Your username" /></Field><Field label="Password"><input required minLength="6" type="password" value={form.password} onChange={e => setForm({...form, password: e.target.value})} placeholder="At least 6 characters" /></Field>
        {mode === 'register' && <><Field label="Role"><select value={form.userType} onChange={e => setForm({...form, userType: e.target.value})}><option value="parent">Parent</option><option value="child">Child</option></select></Field>{form.userType === 'child' && <Field label="Family group ID"><input required value={form.familyGroupId} onChange={e => setForm({...form, familyGroupId: e.target.value})} placeholder="Paste the family ID" /></Field>}</>}
        {error && <p className="form-error">{error}</p>}<button className="primary full" disabled={busy}>{busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create profile'} <Icon name="arrow" /></button>
      </form></div>
    </section>
  </div>
}

function Overview({ dashboard, accounts, tasks, setView, setModal }) {
  const upcoming = tasks.filter(task => !['Done', 'Reject'].includes(task.status)).slice(0, 4)
  return <div className="page-grid">
    <section className="hero-card"><div><span className="pill">{dashboard.user.userType === 'parent' ? 'Parent dashboard' : 'My dashboard'}</span><h2>Good to see you, {dashboard.user.userName}.</h2><p>{dashboard.openTasks ? `${dashboard.openTasks} family tasks are moving forward.` : 'Everything is caught up. Nice work!'}</p></div><div className="hero-visual"><span>{dashboard.completedTasks}</span><small>completed</small></div></section>
    <section className="metrics"><Metric label="Total balance" value={money(dashboard.totalBalance)} note="Across active accounts" accent="blue"/><Metric label="Open tasks" value={dashboard.openTasks} note="Awaiting family action" accent="orange"/><Metric label="Family members" value={dashboard.familyMembers.length} note="In this shared space" accent="green"/></section>
    <section className="card span-2"><div className="section-head"><div><p className="eyebrow">Momentum</p><h3>Upcoming tasks</h3></div><button className="text-button" onClick={() => setView('tasks')}>View all <Icon name="arrow" /></button></div>{upcoming.length ? <div className="task-list compact">{upcoming.map(task => <TaskRow key={task.taskId} task={task}/>)}</div> : <Empty title="No open tasks" text="Create a task and give the week some momentum."/>}</section>
    <section className="card"><div className="section-head"><div><p className="eyebrow">Wallet</p><h3>Accounts</h3></div><button className="round-button" onClick={() => setModal('account')}><Icon name="plus" /></button></div><div className="balance-total">{money(dashboard.totalBalance)}</div><div className="mini-accounts">{accounts.slice(0, 3).map(account => <div key={account.accountId}><span>{account.accountType}</span><strong>{money(account.balance)}</strong></div>)}</div></section>
  </div>
}

function TasksPage({ tasks, user, status, setStatus, sort, setSort, mutate, setModal }) {
  const transition = (task, target, message) => mutate(() => api(`/api/tasks/${task.taskId}/status`, { method: 'PATCH', body: JSON.stringify({ status: target }) }), message)
  return <section className="card page-card"><div className="filterbar"><div className="status-tabs">{Object.entries(STATUS).map(([value, label]) => <button key={value} className={status === value ? 'active' : ''} onClick={() => setStatus(value)}>{label}</button>)}</div><select value={sort} onChange={e => setSort(e.target.value)}><option value="deadline">Deadline</option><option value="reward">Highest reward</option><option value="urgency">Urgency</option></select></div>
    {tasks.length ? <div className="task-list">{tasks.map(task => <div className="task-row detailed" key={task.taskId}><TaskRow task={task}/><div className="row-actions">{user.userType === 'parent' && task.status === 'WaitforCheck' && <><button className="ghost" onClick={() => transition(task, 'Reject', 'Task rejected.')}>Reject</button><button className="primary small" onClick={() => transition(task, 'ToDo', 'Task approved.')}>Approve</button></>}{user.userType === 'parent' && task.status === 'WaitforConfirm' && <button className="primary small" onClick={() => setModal({ type: 'confirm', task })}>Confirm & pay</button>}{user.userType === 'child' && task.status === 'ToDo' && <button className="primary small" onClick={() => transition(task, 'Doing', 'Task accepted.')}>Start</button>}{user.userType === 'child' && task.status === 'Doing' && <button className="primary small" onClick={() => transition(task, 'WaitforConfirm', 'Sent for confirmation.')}>Complete</button>}</div></div>)}</div> : <Empty title="No tasks here" text="Try another filter or create a new task."/>}
  </section>
}

function AccountsPage({ accounts, setModal }) {
  return <><div className="section-head accounts-head"><div><p className="eyebrow">Virtual banking</p><h2>Your accounts</h2></div><button className="primary" onClick={() => setModal('account')}><Icon name="plus" /> New account</button></div><div className="account-grid">{accounts.map((account, index) => <article className={`account-card tone-${index % 3}`} key={account.accountId}><div className="account-top"><span>{account.accountType}</span><span className="chip">••••</span></div><strong>{money(account.balance)}</strong><small>{account.accountId}</small><div className="account-actions"><button onClick={() => setModal({ type: 'money', action: 'deposit', account })}>Deposit</button><button onClick={() => setModal({ type: 'money', action: 'withdraw', account })}>Withdraw</button><button onClick={() => setModal({ type: 'money', action: 'transfer', account })}>Transfer</button></div></article>)}</div>{!accounts.length && <Empty title="No accounts yet" text="Create a checking or fixed-deposit account to get started."/>}</>
}

function FamilyPage({ dashboard }) {
  const copy = () => navigator.clipboard.writeText(dashboard.user.familyGroupId)
  return <section className="card page-card"><div className="family-code"><div><p className="eyebrow">Invite code</p><h3>{dashboard.user.familyGroupId}</h3><p>Share this ID with a child so they can join your family space.</p></div><button className="primary" onClick={copy}>Copy ID</button></div><div className="member-grid">{dashboard.familyMembers.map(member => <div className="member" key={member.userId}><Avatar name={member.userName}/><div><strong>{member.userName}</strong><small>{member.userType}</small></div></div>)}</div></section>
}

function TaskModal({ members, onClose, onSave }) {
  const [form, setForm] = useState({ name: '', description: '', urgency: 2, repeat: 'None', reward: 0, maxBonus: 0, startTime: localInput(new Date()), endTime: localInput(new Date(Date.now() + 86400000)), assigneeId: '', collaboratorId: '' })
  return <Modal title="Create a new task" subtitle="Set clear expectations and a meaningful reward." onClose={onClose}><form onSubmit={e => { e.preventDefault(); onSave({...form, reward: +form.reward, maxBonus: +form.maxBonus, urgency: +form.urgency}) }} className="form-grid"><Field label="Task name" wide><input required value={form.name} onChange={e => setForm({...form, name: e.target.value})}/></Field><Field label="Description" wide><textarea value={form.description} onChange={e => setForm({...form, description: e.target.value})}/></Field><Field label="Assignee"><select value={form.assigneeId} onChange={e => setForm({...form, assigneeId: e.target.value})}><option value="">Unassigned</option>{members.map(m => <option key={m.userId} value={m.userId}>{m.userName}</option>)}</select></Field><Field label="Collaborator"><select value={form.collaboratorId} onChange={e => setForm({...form, collaboratorId: e.target.value})}><option value="">None</option>{members.map(m => <option key={m.userId} value={m.userId}>{m.userName}</option>)}</select></Field><Field label="Starts"><input type="datetime-local" value={form.startTime} onChange={e => setForm({...form, startTime: e.target.value})}/></Field><Field label="Deadline"><input type="datetime-local" value={form.endTime} onChange={e => setForm({...form, endTime: e.target.value})}/></Field><Field label="Reward"><input min="0" step="0.01" type="number" value={form.reward} onChange={e => setForm({...form, reward: e.target.value})}/></Field><Field label="Maximum bonus"><input min="0" step="0.01" type="number" value={form.maxBonus} onChange={e => setForm({...form, maxBonus: e.target.value})}/></Field><Field label="Urgency"><select value={form.urgency} onChange={e => setForm({...form, urgency: e.target.value})}>{[0,1,2,3,4,5].map(value => <option key={value}>{value}</option>)}</select></Field><Field label="Repeat"><select value={form.repeat} onChange={e => setForm({...form, repeat: e.target.value})}>{['None','Daily','Weekly','Monthly'].map(value => <option key={value}>{value}</option>)}</select></Field><ModalActions onClose={onClose} label="Create task"/></form></Modal>
}

function AccountModal({ onClose, onSave }) {
  const [form, setForm] = useState({ accountType: 'Checking', password: '', interestRate: 0.03 })
  return <Modal title="Create an account" subtitle="A safe virtual account for learning healthy money habits." onClose={onClose}><form onSubmit={e => {e.preventDefault(); onSave({...form, interestRate: +form.interestRate})}}><Field label="Account type"><select value={form.accountType} onChange={e => setForm({...form, accountType: e.target.value})}><option value="Checking">Checking</option><option value="FixedDeposit">Fixed deposit</option></select></Field>{form.accountType === 'FixedDeposit' && <Field label="Annual interest rate"><input type="number" min="0" max="1" step="0.01" value={form.interestRate} onChange={e => setForm({...form, interestRate: e.target.value})}/></Field>}<Field label="Account password"><input required minLength="4" type="password" value={form.password} onChange={e => setForm({...form, password: e.target.value})}/></Field><ModalActions onClose={onClose} label="Create account"/></form></Modal>
}

function MoneyModal({ data, onClose, onSave }) {
  const [amount, setAmount] = useState(''); const [password, setPassword] = useState(''); const [target, setTarget] = useState('')
  const action = data.action
  return <Modal title={`${action[0].toUpperCase()}${action.slice(1)} funds`} subtitle={`${data.account.accountType} · ${data.account.accountId}`} onClose={onClose}><form onSubmit={e => {e.preventDefault(); onSave(`/api/accounts/${data.account.accountId}/${action}`, action === 'transfer' ? { amount:+amount, password, targetAccountId:target } : { amount:+amount, password })}}>{action === 'transfer' && <Field label="Target account ID"><input required value={target} onChange={e => setTarget(e.target.value)}/></Field>}<Field label="Amount"><input required autoFocus type="number" min="0.01" step="0.01" value={amount} onChange={e => setAmount(e.target.value)}/></Field>{action !== 'deposit' && <Field label="Account password"><input required type="password" value={password} onChange={e => setPassword(e.target.value)}/></Field>}<ModalActions onClose={onClose} label="Confirm"/></form></Modal>
}

function ConfirmModal({ task, onClose, onSave }) { const [bonus, setBonus] = useState(0); return <Modal title="Confirm completion" subtitle={`Reward ${money(task.reward)} · bonus up to ${money(task.maxBonus)}`} onClose={onClose}><Field label={`Bonus: ${money(bonus)}`}><input type="range" min="0" max={task.maxBonus} step="0.01" value={bonus} onChange={e => setBonus(+e.target.value)}/></Field><div className="payout"><span>Total payout</span><strong>{money(task.reward + bonus)}</strong></div><div className="modal-actions"><button className="ghost" onClick={onClose}>Cancel</button><button className="primary" onClick={() => onSave(bonus)}>Confirm & pay</button></div></Modal> }

function TaskRow({ task }) { return <div className="task-main"><div className={`status-dot status-${task.status}`} /><div><strong>{task.name}</strong><p>{task.assigneeName || 'Unassigned'} · {dateTime(task.endTime)}</p></div><div className="task-meta"><span className="status-label">{STATUS[task.status] || task.status}</span><strong>{money((task.reward || 0) + (task.bonus || 0))}</strong></div></div> }
function Metric({ label, value, note, accent }) { return <article className={`metric ${accent}`}><span>{label}</span><strong>{value}</strong><small>{note}</small></article> }
function Empty({ title, text }) { return <div className="empty"><div>✓</div><h3>{title}</h3><p>{text}</p></div> }
function Nav({ active, icon, label, count, onClick }) { return <button className={active ? 'active' : ''} onClick={onClick}><Icon name={icon}/><span>{label}</span>{count > 0 && <b>{count}</b>}</button> }
function Avatar({ name }) { return <div className="avatar">{(name || '?').slice(0, 2).toUpperCase()}</div> }
function Field({ label, children, wide }) { return <label className={wide ? 'wide' : ''}><span>{label}</span>{children}</label> }
function Modal({ title, subtitle, onClose, children }) { return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}><div className="modal"><button className="modal-close" onClick={onClose}><Icon name="close"/></button><p className="eyebrow">FamilyFlow</p><h2>{title}</h2><p className="subtle">{subtitle}</p>{children}</div></div> }
function ModalActions({ onClose, label }) { return <div className="modal-actions wide"><button type="button" className="ghost" onClick={onClose}>Cancel</button><button className="primary">{label}</button></div> }
function titleFor(view) { return ({ overview: 'Overview', tasks: 'Family tasks', accounts: 'Virtual accounts', family: 'Your family' })[view] }
function localInput(date) { const offset = date.getTimezoneOffset(); return new Date(date.getTime() - offset * 60000).toISOString().slice(0, 16) }

export default App
