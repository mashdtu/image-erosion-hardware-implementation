import numpy as np
import matplotlib.pyplot as plt
from scipy import stats

def create_beta_distribution(minimum, average, maximum):
    # Normalize the average to [0, 1] range
    range_val = maximum - minimum
    normalized_mean = (average - minimum) / range_val
    
    # For beta distribution, mean = alpha / (alpha + beta)
    # Assume a moderate variance that gives a nice bell shape
    # Variance for beta = (alpha * beta) / ((alpha + beta)^2 * (alpha + beta + 1))
    
    center_deviation = abs(normalized_mean - 0.5)
    
    if center_deviation < 0.1:  # Close to center - more symmetric
        # For symmetric-ish case
        alpha = beta = 3.0  # This gives a nice bell shape
        # Adjust based on actual mean
        if normalized_mean > 0.5:
            alpha = 3.0 + (normalized_mean - 0.5) * 4
            beta = 3.0
        else:
            alpha = 3.0
            beta = 3.0 + (0.5 - normalized_mean) * 4
    else:
        # More skewed case
        if normalized_mean < 0.5:
            # Skewed right (peak on left)
            alpha = 2.0
            beta = 2.0 / normalized_mean - 2.0
        else:
            # Skewed left (peak on right)
            beta = 2.0
            alpha = 2.0 / (1 - normalized_mean) - 2.0
    
    # Ensure parameters are positive
    alpha = max(alpha, 0.5)
    beta = max(beta, 0.5)
    
    # Scale and location parameters to map [0,1] to [minimum, maximum]
    loc = minimum
    scale = range_val
    
    return alpha, beta, loc, scale

def create_normal_distribution(minimum, average, maximum):
    # Use the average as the mean
    mean = average

    dist_to_min = abs(mean - minimum)
    dist_to_max = abs(mean - maximum)
    max_distance = max(dist_to_min, dist_to_max)
    
    # Set standard deviation so that the furthest point is ~4 sigma away
    std_dev = max_distance / 4.0
    
    return mean, std_dev

def calculate_confidence_interval(minimum, average, maximum, confidence_level=0.97, distribution_type='beta'):
    """
    Calculate the confidence interval for the distribution.
    
    Parameters:
    minimum (float): The minimum value
    average (float): The average/mean value  
    maximum (float): The maximum value
    confidence_level (float): Confidence level (e.g., 0.97 for 97%)
    distribution_type (str): 'beta', 'normal', or 'truncated'
    
    Returns:
    tuple: (lower_bound, upper_bound) of the confidence interval
    """
    alpha_level = (1 - confidence_level) / 2  # For two-tailed interval
    
    if distribution_type == 'beta':
        alpha, beta_param, loc, scale = create_beta_distribution(minimum, average, maximum)
        lower_bound = stats.beta.ppf(alpha_level, alpha, beta_param, loc=loc, scale=scale)
        upper_bound = stats.beta.ppf(1 - alpha_level, alpha, beta_param, loc=loc, scale=scale)
    else:  # normal or truncated
        mean, std_dev = create_normal_distribution(minimum, average, maximum)
        if distribution_type == 'truncated':
            # For truncated normal, we need to account for the truncation
            # Use the truncated normal distribution from scipy
            a, b = (minimum - mean) / std_dev, (maximum - mean) / std_dev
            lower_bound = stats.truncnorm.ppf(alpha_level, a, b, loc=mean, scale=std_dev)
            upper_bound = stats.truncnorm.ppf(1 - alpha_level, a, b, loc=mean, scale=std_dev)
        else:
            # Regular normal distribution
            lower_bound = stats.norm.ppf(alpha_level, mean, std_dev)
            upper_bound = stats.norm.ppf(1 - alpha_level, mean, std_dev)
    
    return lower_bound, upper_bound

def create_truncated_normal_distribution(minimum, average, maximum, x):
    mean, std_dev = create_normal_distribution(minimum, average, maximum)
    
    # Create normal distribution
    y = stats.norm.pdf(x, mean, std_dev)
    
    # Truncate: set to 0 outside [minimum, maximum]
    y = np.where((x >= minimum) & (x <= maximum), y, 0)
    
    # Normalize so the area under the curve is 1
    if np.trapezoid(y, x) > 0:
        y = y / np.trapezoid(y, x)
    
    return y

def plot_distribution(minimum, average, maximum, num_points=1000, distribution_type='beta', samples=None):
    # Generate x values for plotting
    x_range = maximum - minimum
    x_start = minimum - 0.1 * x_range  # Extend slightly beyond the range
    x_end = maximum + 0.1 * x_range
    x = np.linspace(x_start, x_end, num_points)
    
    # Generate distribution based on type
    if distribution_type == 'beta':
        alpha, beta_param, loc, scale = create_beta_distribution(minimum, average, maximum)
        y = stats.beta.pdf(x, alpha, beta_param, loc=loc, scale=scale)
        # Set values outside [minimum, maximum] to 0
        y = np.where((x >= minimum) & (x <= maximum), y, 0)
        dist_type = f"Beta Distribution (α={alpha:.2f}, β={beta_param:.2f})"
    elif distribution_type == 'truncated':
        y = create_truncated_normal_distribution(minimum, average, maximum, x)
        dist_type = "Truncated Normal"
    else:  # normal
        mean, std_dev = create_normal_distribution(minimum, average, maximum)
        y = stats.norm.pdf(x, mean, std_dev)
        dist_type = f"Normal Distribution (μ={mean:.2f}, σ={std_dev:.2f})"
    
    # Create the plot
    plt.figure(figsize=(12, 7))
    
    # Add histogram if samples are provided
    if samples is not None:
        plt.hist(samples, bins=50, density=True, alpha=0.6, color='skyblue', 
                label='Example Histogram', edgecolor='white', linewidth=0.5)
    
    # Plot the theoretical distribution
    plt.plot(x, y, 'b-', linewidth=3, label=dist_type)
    
    # Mark the three input points
    plt.axvline(minimum, color='red', linestyle='--', alpha=0.7, label=f'Minimum: {minimum}')
    plt.axvline(average, color='green', linestyle='--', alpha=0.7, label=f'Average: {average}')
    plt.axvline(maximum, color='red', linestyle='--', alpha=0.7, label=f'Maximum: {maximum}')
    
    # Add markers for the three points on the curve (without labels)
    # Find the y-values at the input points
    min_idx = np.argmin(np.abs(x - minimum))
    avg_idx = np.argmin(np.abs(x - average))
    max_idx = np.argmin(np.abs(x - maximum))
    
    min_y = y[min_idx] if min_idx < len(y) else 0
    avg_y = y[avg_idx] if avg_idx < len(y) else 0
    max_y = y[max_idx] if max_idx < len(y) else 0
    
    # Plot points without adding them to legend
    plt.plot(minimum, min_y, 'ro', markersize=8)
    plt.plot(average, avg_y, 'go', markersize=8)
    plt.plot(maximum, max_y, 'ro', markersize=8)
    
    # Calculate and show 97% confidence interval
    ci_lower, ci_upper = calculate_confidence_interval(minimum, average, maximum, 0.97, distribution_type)
    plt.axvline(ci_lower, color='orange', linestyle=':', alpha=0.8, linewidth=2, label=f'97% CI Lower: {ci_lower:.1f}')
    plt.axvline(ci_upper, color='orange', linestyle=':', alpha=0.8, linewidth=2, label=f'97% CI Upper: {ci_upper:.1f}')
    
    # Fill the area between the confidence interval bounds
    x_ci = x[(x >= ci_lower) & (x <= ci_upper)]
    y_ci = y[(x >= ci_lower) & (x <= ci_upper)]
    if len(x_ci) > 0:
        plt.fill_between(x_ci, y_ci, alpha=0.2, color='orange', label='97% Confidence Interval')
    
    # Formatting
    plt.xlabel('Value')
    plt.ylabel('Probability Density')
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    
    # Print statistics
    print(f"Distribution Parameters:")
    if distribution_type == 'beta':
        alpha, beta_param, loc, scale = create_beta_distribution(minimum, average, maximum)
        print(f"Alpha (α): {alpha:.4f}")
        print(f"Beta (β): {beta_param:.4f}")
        print(f"Location: {loc:.4f}")
        print(f"Scale: {scale:.4f}")
        # Calculate actual mean and std of the beta distribution
        actual_mean = loc + scale * alpha / (alpha + beta_param)
        actual_var = scale**2 * (alpha * beta_param) / ((alpha + beta_param)**2 * (alpha + beta_param + 1))
        print(f"Actual Mean: {actual_mean:.4f}")
        print(f"Actual Std: {np.sqrt(actual_var):.4f}")
    else:
        mean, std_dev = create_normal_distribution(minimum, average, maximum)
        print(f"Mean (μ): {mean:.4f}")
        print(f"Standard Deviation (σ): {std_dev:.4f}")
    
    print(f"\nInput Points:")
    print(f"Minimum: {minimum}")
    print(f"Average: {average}")
    print(f"Maximum: {maximum}")
    print(f"\nProbability densities at input points:")
    print(f"P(X={minimum}): {min_y:.6f}")
    print(f"P(X={average}): {avg_y:.6f}")
    print(f"P(X={maximum}): {max_y:.6f}")
    
    # Calculate and print confidence intervals
    ci_lower, ci_upper = calculate_confidence_interval(minimum, average, maximum, 0.97, distribution_type)
    ci_width = ci_upper - ci_lower
    ci_center = (ci_lower + ci_upper) / 2
    
    print(f"\n97% Confidence Interval:")
    print(f"Lower bound: {ci_lower:.2f}")
    print(f"Upper bound: {ci_upper:.2f}")
    print(f"Interval width: {ci_width:.2f}")
    print(f"Interval center: {ci_center:.2f}")
    print(f"97% of the probability mass lies between {ci_lower:.2f} and {ci_upper:.2f}")
    
    # Also calculate other common intervals
    ci_90_lower, ci_90_upper = calculate_confidence_interval(minimum, average, maximum, 0.90, distribution_type)
    ci_95_lower, ci_95_upper = calculate_confidence_interval(minimum, average, maximum, 0.95, distribution_type)
    ci_99_lower, ci_99_upper = calculate_confidence_interval(minimum, average, maximum, 0.99, distribution_type)
    
    print(f"\nOther Confidence Intervals:")
    print(f"90% CI: [{ci_90_lower:.2f}, {ci_90_upper:.2f}] (width: {ci_90_upper-ci_90_lower:.2f})")
    print(f"95% CI: [{ci_95_lower:.2f}, {ci_95_upper:.2f}] (width: {ci_95_upper-ci_95_lower:.2f})")
    print(f"97% CI: [{ci_lower:.2f}, {ci_upper:.2f}] (width: {ci_width:.2f})")
    print(f"99% CI: [{ci_99_lower:.2f}, {ci_99_upper:.2f}] (width: {ci_99_upper-ci_99_lower:.2f})")
    
    plt.show()
    
    return x, y

def generate_samples(minimum, average, maximum, num_samples=1000, distribution_type='beta'):
    if distribution_type == 'beta':
        alpha, beta_param, loc, scale = create_beta_distribution(minimum, average, maximum)
        samples = stats.beta.rvs(alpha, beta_param, loc=loc, scale=scale, size=num_samples)
    else:
        mean, std_dev = create_normal_distribution(minimum, average, maximum)
        samples = np.random.normal(mean, std_dev, num_samples)
        if distribution_type == 'truncated':
            # Keep only samples within bounds
            samples = samples[(samples >= minimum) & (samples <= maximum)]
            # If we dont have enough samples, generate more
            while len(samples) < num_samples:
                additional = np.random.normal(mean, std_dev, num_samples - len(samples))
                valid_additional = additional[(additional >= minimum) & (additional <= maximum)]
                samples = np.concatenate([samples, valid_additional])
            samples = samples[:num_samples]  # Trim to exact size
    
    return samples

if __name__ == "__main__":
    min_val = 4507741
    avg_val = 6845310
    max_val = 17116113
    
    print("Creating normal distribution from three points:")
    print(f"Minimum: {min_val}")
    print(f"Average: {avg_val}")
    print(f"Maximum: {max_val}")
    print("-" * 40)
    
    # Generate some sample data first
    print(f"\nGenerating 1000 random samples...")
    samples = generate_samples(min_val, avg_val, max_val, 1000, distribution_type='beta')
    print(f"Sample statistics:")
    print(f"Sample mean: {np.mean(samples):.4f}")
    print(f"Sample std: {np.std(samples, ddof=1):.4f}")
    print(f"Sample min: {np.min(samples):.4f}")
    print(f"Sample max: {np.max(samples):.4f}")
    
    # Calculate empirical 97% confidence interval from samples
    sample_ci_lower = np.percentile(samples, 1.5)  # 1.5th percentile for 97% CI
    sample_ci_upper = np.percentile(samples, 98.5)  # 98.5th percentile for 97% CI
    print(f"\nEmpirical 97% CI from samples:")
    print(f"Sample CI: [{sample_ci_lower:.2f}, {sample_ci_upper:.2f}]")
    
    # Plot the distribution with histogram included
    x, y = plot_distribution(min_val, avg_val, max_val, distribution_type='beta', samples=samples)
