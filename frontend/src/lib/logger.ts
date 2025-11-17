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
    private readonly isClient: boolean;

    constructor() {
        this.isDev = process.env.NODE_ENV === Environment.DEVELOPMENT;
        this.isClient = typeof window !== "undefined";
    }

    private formatMessage(level: LogLevel, message: string, context?: LogContext): string {
        const timestamp = new Date().toISOString();
        const env = this.isClient ? "client" : "server";
        return `[${timestamp}] [${level.toUpperCase()}] [${env}] ${message}`;
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

    d(message: string, context?: LogContext) { this.debug(message, context) }
    i(message: string, context?: LogContext) { this.info(message, context) }
    w(message: string, context?: LogContext) { this.warn(message, context) }
    e(message: string, error?: Error, context?: LogContext) { this.error(message, error, context) }
}

export const logger = new Logger();