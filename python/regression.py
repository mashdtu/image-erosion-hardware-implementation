import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit

# Define data points
x = np.array([5, 10, 15, 20])
y = np.array([91, 441, 1041, 1891])
#x = np.array([10, 15, 20])
#y = np.array([1493, 3678, 6903])

# Define model functions
def linear(x, m, c):
    return m * x + c

def exponential(x, a, b):
    return a * np.exp(b * x)

def power_law(x, a, b):
    return a * x**b

# Compute R^2 values
def compute_r_squared(y_true, y_pred):
    residuals = y_true - y_pred
    ss_res = np.sum(residuals**2)
    ss_tot = np.sum((y_true - np.mean(y_true))**2)
    return 1 - (ss_res / ss_tot)

# Fit models
lin_params, _ = curve_fit(linear, x, y)
exp_params, _ = curve_fit(exponential, x, y, p0=(1, 0.01), maxfev=10000)
pow_params, _ = curve_fit(power_law, x, y)

# Generate smooth x for plotting
x_smooth = np.linspace(min(x), max(x), 500)

# Compute fitted curves
y_lin = linear(x_smooth, *lin_params)
y_exp = exponential(x_smooth, *exp_params)
y_pow = power_law(x_smooth, *pow_params)

# Predictions on original x
y_lin_pred = linear(x, *lin_params)
y_exp_pred = exponential(x, *exp_params)
y_pow_pred = power_law(x, *pow_params)

# R^2 values
r2_lin = compute_r_squared(y, y_lin_pred)
r2_exp = compute_r_squared(y, y_exp_pred)
r2_pow = compute_r_squared(y, y_pow_pred)

# Print R^2 values
print(f"Linear R²: {r2_lin:.10f}")
print(f"Exponential R²: {r2_exp:.10f}")
print(f"Power-Law R²: {r2_pow:.10f}")

# Format labels with equations and R^2
lin_label = f"Linear (R² = {r2_lin:.6f})\ny = {lin_params[0]:.2f}x + {lin_params[1]:.2f}"
exp_label = f"Exponential (R² = {r2_exp:.6f})\ny = {exp_params[0]:.2e}·e^({exp_params[1]:.2e}x)"
pow_label = f"Power-Law (R² = {r2_pow:.6f})\ny = {pow_params[0]:.2e}·x^{pow_params[1]:.2f}"


# Plotting
plt.figure(figsize=(10, 6))
plt.scatter(x, y, color='red', label='Data Points', zorder=5)
plt.plot(x_smooth, y_lin, label=lin_label)
plt.plot(x_smooth, y_exp, label=exp_label)
plt.plot(x_smooth, y_pow, label=pow_label)

# Labels and legend
plt.xlabel('x')
plt.ylabel('y')
plt.legend()
plt.grid(True)
plt.tight_layout()
plt.show()