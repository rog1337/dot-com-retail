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
    [key: string]: unknown
}

class Logger {
    private readonly isDev: boolean

    constructor() {
        this.isDev = process.env.NODE_ENV === Environment.DEVELOPMENT
    }

    private formatMessage(level: LogLevel, message: string, context?: unknown): string {
        const timestamp = new Date().toISOString()
        return `[${timestamp}] [${level.toUpperCase()}] ${message}${context ? "\n" : ""}`
    }

    private shouldLog(level: LogLevel): boolean {
        if (this.isDev) return true
        return level === LogLevel.INFO || level === LogLevel.WARNING || level === LogLevel.ERROR
    }

    debug(message: string, context?: unknown) {
        if (this.shouldLog(LogLevel.DEBUG)) {
            console.debug(this.formatMessage(LogLevel.DEBUG, message, context), context || "")
        }
    }

    info(message: string, context?: unknown) {
        if (this.shouldLog(LogLevel.INFO)) {
            console.info(this.formatMessage(LogLevel.INFO, message, context), context || "")
        }
    }

    warn(message: string, context?: LogContext) {
        if (this.shouldLog(LogLevel.WARNING)) {
            console.warn(this.formatMessage(LogLevel.WARNING, message, context), context || "")
        }
    }

    error(message: string, error?: Error, context?: LogContext) {
        if (this.shouldLog(LogLevel.ERROR)) {
            console.error(this.formatMessage(LogLevel.ERROR, message, context), {
                error: error?.message,
                ...context,
            })
        }
    }

    api(method: string, url: string, status: number, data: unknown = "") {
        const message = `[API] ${method.toUpperCase()} ${url}${status ? ` - ${status}` : ""}`
        const context = { method, url, status }

        this.debug(message, { ...context, data })

        // if (status >= 400) {
        //     this.error(message,undefined,context)
        // } else if (this.isDev) {
        //     this.info(message, context)
        // }
    }

    d(message: string, context?: unknown) { this.debug(message, context) }
    i(message: string, context?: LogContext) { this.info(message, context) }
    w(message: string, context?: LogContext) { this.warn(message, context) }
    e(message: string, error?: Error, context?: LogContext) { this.error(message, error, context) }
}

export const logger = new Logger()