/**
 * Main JavaScript file for Banking System
 */

// Initialize tooltips and popovers
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize Bootstrap popovers
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Auto-dismiss alerts after 5 seconds
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            var bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 5000);
    
    // Format currency inputs
    var currencyInputs = document.querySelectorAll('.currency-input');
    currencyInputs.forEach(function(input) {
        input.addEventListener('input', function(e) {
            // Remove non-numeric characters except decimal point
            let value = e.target.value.replace(/[^\d.]/g, '');
            
            // Ensure only one decimal point
            let parts = value.split('.');
            if (parts.length > 2) {
                value = parts[0] + '.' + parts.slice(1).join('');
            }
            
            // Limit to 2 decimal places
            if (parts.length > 1) {
                parts[1] = parts[1].substring(0, 2);
                value = parts[0] + '.' + parts[1];
            }
            
            e.target.value = value;
        });
    });
    
    // Format date inputs to ISO format for backend
    var dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(function(input) {
        input.addEventListener('change', function(e) {
            const date = new Date(e.target.value);
            if (!isNaN(date.getTime())) {
                const isoDate = date.toISOString().split('T')[0];
                e.target.setAttribute('data-iso-date', isoDate);
            }
        });
    });
    
    // Add confirmation for dangerous actions
    var dangerousActions = document.querySelectorAll('.needs-confirmation');
    dangerousActions.forEach(function(element) {
        element.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to perform this action?')) {
                e.preventDefault();
            }
        });
    });
});

/**
 * Format a number as currency
 * @param {number} amount - The amount to format
 * @param {string} currency - The currency code (default: USD)
 * @returns {string} Formatted currency string
 */
function formatCurrency(amount, currency = 'USD') {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: currency
    }).format(amount);
}

/**
 * Format a date
 * @param {string} dateString - The date string to format
 * @param {boolean} includeTime - Whether to include time in the formatted string
 * @returns {string} Formatted date string
 */
function formatDate(dateString, includeTime = false) {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) {
        return 'Invalid date';
    }
    
    const options = {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    };
    
    if (includeTime) {
        options.hour = '2-digit';
        options.minute = '2-digit';
    }
    
    return date.toLocaleDateString('en-US', options);
}

/**
 * Create a chart
 * @param {string} elementId - The ID of the canvas element
 * @param {string} type - The type of chart (bar, line, pie, etc.)
 * @param {Object} data - The data for the chart
 * @param {Object} options - The options for the chart
 * @returns {Chart} The created chart
 */
function createChart(elementId, type, data, options) {
    const element = document.getElementById(elementId);
    if (!element) {
        console.error(`Element with ID '${elementId}' not found`);
        return null;
    }
    
    return new Chart(element, {
        type: type,
        data: data,
        options: options
    });
}

/**
 * Initialize dashboard charts
 */
function initDashboardCharts() {
    // Balance History Chart
    if (document.getElementById('balanceHistoryChart')) {
        const balanceData = {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'Balance',
                data: [5000, 5500, 5200, 6000, 5800, 6500],
                borderColor: '#0d6efd',
                backgroundColor: 'rgba(13, 110, 253, 0.1)',
                fill: true,
                tension: 0.4
            }]
        };
        
        const balanceOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return formatCurrency(context.raw);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrency(value);
                        }
                    }
                }
            }
        };
        
        createChart('balanceHistoryChart', 'line', balanceData, balanceOptions);
    }
    
    // Expense Categories Chart
    if (document.getElementById('expenseCategoriesChart')) {
        const expenseData = {
            labels: ['Housing', 'Food', 'Transportation', 'Utilities', 'Entertainment', 'Other'],
            datasets: [{
                data: [1200, 500, 300, 200, 150, 100],
                backgroundColor: [
                    '#0d6efd',
                    '#6610f2',
                    '#6f42c1',
                    '#d63384',
                    '#dc3545',
                    '#fd7e14'
                ]
            }]
        };
        
        const expenseOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right'
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.label + ': ' + formatCurrency(context.raw);
                        }
                    }
                }
            }
        };
        
        createChart('expenseCategoriesChart', 'pie', expenseData, expenseOptions);
    }
    
    // Income vs Expenses Chart
    if (document.getElementById('incomeExpensesChart')) {
        const incomeExpenseData = {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [
                {
                    label: 'Income',
                    data: [3000, 3200, 3100, 3400, 3300, 3500],
                    backgroundColor: '#198754'
                },
                {
                    label: 'Expenses',
                    data: [2500, 2700, 2600, 2800, 2750, 2900],
                    backgroundColor: '#dc3545'
                }
            ]
        };
        
        const incomeExpenseOptions = {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + formatCurrency(context.raw);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatCurrency(value);
                        }
                    }
                }
            }
        };
        
        createChart('incomeExpensesChart', 'bar', incomeExpenseData, incomeExpenseOptions);
    }
}

/**
 * Initialize loan calculator
 */
function initLoanCalculator() {
    const loanCalculator = document.getElementById('loanCalculatorForm');
    if (!loanCalculator) return;
    
    const amountInput = document.getElementById('loanAmount');
    const interestInput = document.getElementById('interestRate');
    const termInput = document.getElementById('loanTerm');
    const calculateBtn = document.getElementById('calculateLoanBtn');
    const emiResult = document.getElementById('emiResult');
    const totalResult = document.getElementById('totalResult');
    const interestResult = document.getElementById('interestResult');
    
    calculateBtn.addEventListener('click', function(e) {
        e.preventDefault();
        
        const amount = parseFloat(amountInput.value);
        const interest = parseFloat(interestInput.value);
        const term = parseInt(termInput.value);
        
        if (isNaN(amount) || isNaN(interest) || isNaN(term)) {
            alert('Please enter valid values for all fields');
            return;
        }
        
        // Calculate EMI: [P x R x (1+R)^N]/[(1+R)^N-1]
        // P = Principal, R = Monthly Interest Rate, N = Number of Months
        const monthlyInterest = interest / 1200; // Convert annual percentage to monthly decimal
        const numerator = amount * monthlyInterest * Math.pow(1 + monthlyInterest, term);
        const denominator = Math.pow(1 + monthlyInterest, term) - 1;
        const emi = numerator / denominator;
        
        const totalAmount = emi * term;
        const totalInterest = totalAmount - amount;
        
        emiResult.textContent = formatCurrency(emi);
        totalResult.textContent = formatCurrency(totalAmount);
        interestResult.textContent = formatCurrency(totalInterest);
        
        document.getElementById('loanResultSection').classList.remove('d-none');
    });
}

// Initialize features when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initDashboardCharts();
    initLoanCalculator();
});
