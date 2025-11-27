// 页面加载时检查登录状态
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('hxytt_token');
    if (!token) {
        alert('请先登录后再访问此页面');
        window.location.href = '../login.html';
        return;
    }
});

// 全局变量
let currentPage = 1;
let pageSize = 20;
let currentUserId = null;
let totalRecords = 0;
let totalPages = 0;

// DOM元素
const userIdSearch = document.getElementById('userIdSearch');
const searchBtn = document.getElementById('searchBtn');
const clearBtn = document.getElementById('clearBtn');
const pageSizeSelect = document.getElementById('pageSizeSelect');
const loadingState = document.getElementById('loadingState');
const tableContainer = document.getElementById('tableContainer');
const tableBody = document.getElementById('tableBody');
const paginationContainer = document.getElementById('paginationContainer');
const paginationInfo = document.getElementById('paginationInfo');
const pageNumbers = document.getElementById('pageNumbers');
const prevPageBtn = document.getElementById('prevPageBtn');
const nextPageBtn = document.getElementById('nextPageBtn');

// 统计概览元素
const totalRecordsEl = document.getElementById('totalRecords');
const avgGlossaryCountEl = document.getElementById('avgGlossaryCount');
const avgMatchedTermsEl = document.getElementById('avgMatchedTerms');
const activeUsersEl = document.getElementById('activeUsers');

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    loadStatistics();
    bindEvents();
});

// 绑定事件
function bindEvents() {
    // 搜索按钮
    searchBtn.addEventListener('click', function() {
        currentUserId = userIdSearch.value.trim() || null;
        currentPage = 1;
        loadStatistics();
    });

    // 清空按钮
    clearBtn.addEventListener('click', function() {
        userIdSearch.value = '';
        currentUserId = null;
        currentPage = 1;
        loadStatistics();
    });

    // 回车搜索
    userIdSearch.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchBtn.click();
        }
    });

    // 每页大小变化
    pageSizeSelect.addEventListener('change', function() {
        pageSize = parseInt(this.value);
        currentPage = 1;
        loadStatistics();
    });

    // 分页按钮
    prevPageBtn.addEventListener('click', function() {
        if (currentPage > 1) {
            currentPage--;
            loadStatistics();
        }
    });

    nextPageBtn.addEventListener('click', function() {
        if (currentPage < totalPages) {
            currentPage++;
            loadStatistics();
        }
    });
}

// 加载统计数据
async function loadStatistics() {
    try {
        showLoading(true);
        
        // 构建请求参数
        const params = new URLSearchParams({
            pageNum: currentPage,
            pageSize: pageSize
        });
        
        if (currentUserId) {
            params.append('userId', currentUserId);
        }

        // 调用API
        console.log('API请求URL:', `${window.API_BASE}/translate/statistics?${params}`);
        console.log('完整URL:', window.API_BASE + '/translate/statistics?' + params);

        const headers = {
            'Content-Type': 'application/json'
        };

        const authToken = getAuthToken();
        if (authToken) {
            headers['Authorization'] = authToken;
            console.log('使用认证令牌:', authToken.substring(0, 20) + '...');
        } else {
            console.log('未找到认证令牌');
        }

        const fullUrl = `${window.API_BASE}/translate/statistics?${params}`;
        console.log('发起请求到:', fullUrl);

        const response = await fetch(fullUrl, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const result = await response.json();
        
        if (result.code === 1000 && result.data) {
            const pageData = result.data;
            totalRecords = pageData.total;
            totalPages = pageData.pages;
            
            // 渲染表格数据
            renderTable(pageData.records);
            
            // 更新分页信息
            updatePagination();
            
            // 更新统计概览
            updateStatisticsOverview(pageData.records);
            
        } else {
            throw new Error(result.msg || '获取数据失败');
        }

    } catch (error) {
        console.error('加载统计数据失败:', error);
        console.error('错误详情:', {
            message: error.message,
            stack: error.stack,
            name: error.name
        });
        showError('加载数据失败: ' + error.message);
    } finally {
        showLoading(false);
    }
}

// 渲染表格数据
function renderTable(records) {
    if (!records || records.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="px-6 py-8 text-center text-gray-500">
                    <i class="fa fa-inbox text-4xl mb-4 block"></i>
                    暂无数据
                </td>
            </tr>
        `;
        return;
    }

    tableBody.innerHTML = records.map(record => `
        <tr class="hover:bg-gray-50">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${record.id}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${record.userId}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                ${record.username || '未知用户'}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getGlossaryCountBadgeClass(record.selectedGlossaryCount)}">
                    ${record.selectedGlossaryCount}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getMatchedTermsBadgeClass(record.matchedTermCount)}">
                    ${record.matchedTermCount}
                </span>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                ${formatDateTime(record.createTime)}
            </td>
        </tr>
    `).join('');
}

// 获取术语库数量徽章样式
function getGlossaryCountBadgeClass(count) {
    if (count === 0) return 'bg-gray-100 text-gray-800';
    if (count === 1) return 'bg-blue-100 text-blue-800';
    if (count === 2) return 'bg-green-100 text-green-800';
    return 'bg-purple-100 text-purple-800';
}

// 获取匹配术语数徽章样式
function getMatchedTermsBadgeClass(count) {
    if (count === 0) return 'bg-gray-100 text-gray-800';
    if (count <= 5) return 'bg-yellow-100 text-yellow-800';
    if (count <= 20) return 'bg-orange-100 text-orange-800';
    return 'bg-red-100 text-red-800';
}

// 格式化日期时间
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

// 更新分页信息
function updatePagination() {
    // 更新分页信息文本
    const startRecord = (currentPage - 1) * pageSize + 1;
    const endRecord = Math.min(currentPage * pageSize, totalRecords);
    paginationInfo.textContent = `显示第 ${startRecord}-${endRecord} 条，共 ${totalRecords} 条记录`;

    // 更新上一页/下一页按钮状态
    prevPageBtn.disabled = currentPage <= 1;
    nextPageBtn.disabled = currentPage >= totalPages;

    // 生成页码按钮
    generatePageNumbers();
}

// 生成页码按钮
function generatePageNumbers() {
    const maxVisiblePages = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }

    pageNumbers.innerHTML = '';
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `px-3 py-1 text-sm border rounded-md transition-colors ${
            i === currentPage 
                ? 'bg-primary text-white border-primary' 
                : 'border-gray-300 hover:bg-gray-50'
        }`;
        pageBtn.textContent = i;
        pageBtn.addEventListener('click', () => {
            currentPage = i;
            loadStatistics();
        });
        pageNumbers.appendChild(pageBtn);
    }
}

// 更新统计概览
function updateStatisticsOverview(records) {
    if (!records || records.length === 0) {
        totalRecordsEl.textContent = '0';
        avgGlossaryCountEl.textContent = '0';
        avgMatchedTermsEl.textContent = '0';
        activeUsersEl.textContent = '0';
        return;
    }

    // 计算统计数据
    const totalGlossaryCount = records.reduce((sum, record) => sum + (record.selectedGlossaryCount || 0), 0);
    const totalMatchedTerms = records.reduce((sum, record) => sum + (record.matchedTermCount || 0), 0);
    const uniqueUsers = new Set(records.map(record => record.userId)).size;

    // 更新显示
    totalRecordsEl.textContent = totalRecords.toLocaleString();
    avgGlossaryCountEl.textContent = (totalGlossaryCount / records.length).toFixed(1);
    avgMatchedTermsEl.textContent = (totalMatchedTerms / records.length).toFixed(1);
    activeUsersEl.textContent = uniqueUsers.toLocaleString();
}

// 显示/隐藏加载状态
function showLoading(show) {
    if (show) {
        loadingState.classList.remove('hidden');
        tableContainer.classList.add('hidden');
        paginationContainer.classList.add('hidden');
    } else {
        loadingState.classList.add('hidden');
        tableContainer.classList.remove('hidden');
        paginationContainer.classList.remove('hidden');
    }
}

// 显示错误信息
function showError(message) {
    tableBody.innerHTML = `
        <tr>
            <td colspan="6" class="px-6 py-8 text-center text-red-500">
                <i class="fa fa-exclamation-triangle text-4xl mb-4 block"></i>
                ${message}
            </td>
        </tr>
    `;
}

// 获取认证令牌
function getAuthToken() {
    const token = window.CommonAuth ? window.CommonAuth.getToken() : localStorage.getItem('hxytt_token');
    if (!token) return '';
    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
}
