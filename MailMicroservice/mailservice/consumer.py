import pika
import json
import sys

from sendEmail import send_email_sync


sys.stdout.reconfigure(line_buffering=True)

def callback(ch, method, properties, body):
    print(" [!] CALLBACK URUCHOMIONY!", flush=True)
    try:
        print(" [x] Received raw message:", body, flush=True)
        text = body.decode('utf-8')
        print(" [x] Decoded message:", text, flush=True)
        data = json.loads(text)
        print(" [x] Parsed JSON:", data, flush=True)

        send_email_sync(data['recipient'], data['subject'], data['content'])
        print(" [*] Email sent.", flush=True)

        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(f" [!] Failed to process message: {e}", flush=True)

def main():
    print("Starting RabbitMQ consumer...", flush=True)
    credentials = pika.PlainCredentials('student', 'student')
    parameters = pika.ConnectionParameters('springproject-rabbitmq-1', 5672, '/', credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    channel.queue_declare(queue='mail_queue', durable=False)
    print(" [*] Queue declared", flush=True)

    channel.basic_consume(queue='mail_queue', on_message_callback=callback, auto_ack=False)

    print(' [*] Waiting for messages. Press CTRL+C to exit.', flush=True)
    channel.start_consuming()

if __name__ == '__main__':
    main()
