import smtplib
from email.message import EmailMessage
import os

def send_email_sync(recipient, subject, content):
    smtp_server = os.getenv('SMTP_SERVER', 'sandbox.smtp.mailtrap.io')
    smtp_port = int(os.getenv('SMTP_PORT', 587))
    smtp_user = 'aa6dd19a126328'
    smtp_password = '44b24165ce6fc7'

    msg = EmailMessage()
    msg.set_content(content)
    msg['Subject'] = subject
    msg['From'] = smtp_user
    msg['To'] = "lukaszbolechow@gmail.com"

    with smtplib.SMTP(smtp_server, smtp_port) as smtp:
        smtp.starttls()
        smtp.login(smtp_user, smtp_password)
        smtp.send_message(msg)