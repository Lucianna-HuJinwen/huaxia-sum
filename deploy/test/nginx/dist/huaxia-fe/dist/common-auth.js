// common-auth.js — 一份自洽的通用认证/登出脚本（复制到项目根）
(() => {
  // 如果页面没有显式设置 window.API_BASE，就用相对路径（可在页面上覆盖）
  window.API_BASE = window.API_BASE || '';

  const TOKEN_KEY = 'hxytt_token';

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }
  function setToken(t) {
    if (t) localStorage.setItem(TOKEN_KEY, t);
    else localStorage.removeItem(TOKEN_KEY);
  }
  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  // 通用 fetch 包装（处理 Authorization header）
  async function apiFetch(path, opts = {}) {
    const url = path.startsWith('http') ? path : (window.API_BASE ? window.API_BASE + path : path);
    const token = getToken();
    opts.headers = opts.headers || {};
    if (token) opts.headers['Authorization'] = token.startsWith("Bearer ") ? token : `Bearer ${token}`;
    // 默认 JSON
    if (!opts.headers['Content-Type'] && opts.body && typeof opts.body === 'object') {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(opts.body);
    }
    try {
      const res = await fetch(url, opts);
      // 如果后端统一 401 表示 token 无效/过期 -> 强制登出
      if (res.status === 401) {
        // try to clear and redirect
        handleLogoutRedirect();
        throw new Error('Unauthorized');
      }
      return res;
    } catch (err) {
      // 网络错误：抛出上层处理或返回 null
      console.warn('apiFetch error', err);
      throw err;
    }
  }

  // 实际登出逻辑：调用 DELETE /user/logout -> 清 token -> 跳 login
  async function doLogout() {
    const logoutPath = '/user/logout'; // Apifox 中的路径
    try {
      // 使用 DELETE 请求（按你提供的接口）
      const res = await apiFetch(logoutPath, { method: 'DELETE' });
      // 如果后端返回 200~299 视为成功
      if (res && res.ok) {
        console.log('logout success');
      } else {
        console.warn('logout request failed or non-ok, will still clear token');
      }
    } catch (err) {
      console.warn('logout network/error, still clearing token', err);
    } finally {
      clearToken();
      handleLogoutRedirect();
    }
  }

  function handleLogoutRedirect() {
    // 跳回登录页面（相对路径）
    // 如果你的项目 login.html 在根目录，使用 '/login.html'，否则相对路径
    // 我用根路径 login.html（你本地可改为 './login.html'）
    window.location.href = '/login.html';
  }

  // 给所有 logout 元素绑定点击事件
  function attachLogoutButtons() {
    const els = Array.from(document.querySelectorAll('[data-action="logout"], .logout-btn'));
    if (!els.length) return;
    els.forEach(el => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
        // 让用户确认（可删）
        const ok = confirm('确定要退出登录吗？');
        if (ok) doLogout();
      });
    });
  }

  // 初始化：自动绑定，暴露工具函数
  window.CommonAuth = {
    getToken, setToken, clearToken, doLogout, apiFetch
  };

  // 等 DOM ready 再绑定（兼容各种页面）
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', attachLogoutButtons);
  } else {
    attachLogoutButtons();
  }

  // 如果需要，页面可以通过 `CommonAuth.doLogout()` 直接调用登出
})();
