/**
 * The boundary to the email provider. Enquiry logic builds an `EmailMessage`; how it is
 * delivered (SMTP to Mailpit locally, Amazon SES or SendGrid later) is an adapter detail.
 */
export interface EmailMessage {
  readonly to: string;
  readonly replyTo?: string;
  readonly subject: string;
  readonly text: string;
  readonly html: string;
}

export interface EmailSender {
  /** Resolves once the provider has accepted the message; rejects otherwise. */
  send(message: EmailMessage): Promise<void>;
}
