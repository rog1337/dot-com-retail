export enum LogLevel {
    DEBUG = "debug",
    INFO = "info",
    WARNING = "warning",
    ERROR = "error",
}

export enum Environment {
    DEVELOPMENT = "development",
    PRODUCTION = "production",
    TEST = "test",
}

interface LogContext {
    [key: string]: any;
}

class Logger {
    private readonly isDev: boolean;

    constructor() {
        this.isDev = process.env.NODE_ENV === Environment.DEVELOPMENT;
    }

    private formatMessage(level: LogLevel, message: string, context?: LogContext): string {
        const timestamp = new Date().toISOString();
        return `[${timestamp}] [${level.toUpperCase()}] ${message}`;
    }

    private shouldLog(level: LogLevel): boolean {
        if (this.isDev) return true;
        return level === LogLevel.INFO || level === LogLevel.WARNING || level === LogLevel.ERROR;
    }

    debug(message: string, context?: LogContext) {
        if (this.shouldLog(LogLevel.DEBUG)) {
            console.debug(this.formatMessage(LogLevel.DEBUG, message), context || "");
        }
    }

    info(message: string, context?: LogContext) {
        if (this.shouldLog(LogLevel.INFO)) {
            console.info(this.formatMessage(LogLevel.INFO, message), context || "");
        }
    }

    warn(message: string, context?: LogContext) {
        if (this.shouldLog(LogLevel.WARNING)) {
            console.warn(this.formatMessage(LogLevel.WARNING, message), context || "");
        }
    }

    error(message: string, error?: Error, context?: LogContext) {
        if (this.shouldLog(LogLevel.ERROR)) {
            console.error(this.formatMessage(LogLevel.ERROR, message), {
                error: error?.message,
                stack: error?.stack,
                ...context,
            });
        }
    }

    api(method: string, url: string, status: number) {
        const message = `[API] ${method.toUpperCase()} ${url} - ${status}`;
        const context = { method, url, status };

        if (status >= 400) {
            this.error(message,undefined,context);
        } else if (this.isDev) {
            this.info(message, context);
        }
    }

    d(message: string, context?: LogContext) { this.debug(message, context) }
    i(message: string, context?: LogContext) { this.info(message, context) }
    w(message: string, context?: LogContext) { this.warn(message, context) }
    e(message: string, error?: Error, context?: LogContext) { this.error(message, error, context) }
}

export const logger = new Logger();