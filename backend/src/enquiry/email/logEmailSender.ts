import type { Logger } from '../../shared/logger.js';
import type { EmailMessage, EmailSender } from './emailSender.js';

/**
 * For running without Docker or an SMTP server (`EMAIL_TRANSPORT=log`): writes the email to the
 * server log instead of sending it. Local use only; the default is SMTP.
 */
export class LogEmailSender implements EmailSender {
  constructor(private readonly logger: Logger) {}

  async send(message: EmailMessage): Promise<void> {
    this.logger.info(
      { to: message.to, subject: message.subject, text: message.text },
      'Email not sent (EMAIL_TRANSPORT=log)',
    );
  }
}
