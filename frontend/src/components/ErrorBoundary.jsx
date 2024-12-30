import { Component } from "react";
import PropTypes from "prop-types";

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
    this.resetErrorBoundary = this.resetErrorBoundary.bind(this);
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error, errorInfo) {
    // Log error details
    console.error("Error Boundary caught an error:", error, errorInfo);
    this.setState({
      errorInfo,
    });
  }

  resetErrorBoundary() {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback(this.state.error, this.resetErrorBoundary);
      }

      return (
        <div className="error-boundary">
          <div className="error-content">
            <h2>{this.props.errorMessage || "Something went wrong"}</h2>
            {this.props.showDetails && (
              <details className="error-details">
                <summary>Error Details</summary>
                <pre>
                  {this.state.error && this.state.error.toString()}
                  <br />
                  {this.state.errorInfo && this.state.errorInfo.componentStack}
                </pre>
              </details>
            )}
            <button onClick={this.resetErrorBoundary} className="retry-button">
              {this.props.retryText || "Try Again"}
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

ErrorBoundary.propTypes = {
  children: PropTypes.node.isRequired,
  fallback: PropTypes.func,
  errorMessage: PropTypes.string,
  showDetails: PropTypes.bool,
  retryText: PropTypes.string,
};

ErrorBoundary.defaultProps = {
  showDetails: process.env.NODE_ENV === "development",
  retryText: "Try Again",
};

export default ErrorBoundary;
