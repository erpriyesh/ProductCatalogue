import type { AppConfig } from '../../config/config.js';
import type { Logger } from '../../shared/logger.js';
import type { EmailSender } from './emailSender.js';
import { LogEmailSender } from './logEmailSender.js';
import { SmtpEmailSender } from './smtpEmailSender.js';

/**
 * Picks the email provider from configuration (EMAIL_TRANSPORT). SMTP covers Mailpit locally and
 * Amazon SES in production. An API-based provider (SES SDK, SendGrid) is a new EmailSender class
 * plus a case here.
 */
export function createEmailSender(config: AppConfig['email'], log: Logger): EmailSender {
  switch (config.transport) {
    case 'smtp':
      return new SmtpEmailSender({ ...config.smtp, from: config.from });
    case 'log':
      return new LogEmailSender(log);
  }
}
