import pika
import json
import os
import redis

redis_host = os.getenv("REDIS_HOST", "redis")
redis_port = int(os.getenv("REDIS_PORT", 6379))

r = redis.Redis(host=redis_host, port=redis_port, db=0)

def callback(ch, method, properties, body):
    print(" [!] Received message", flush=True)
    try:
        data = json.loads(body.decode('utf-8'))
        print(f" [x] Data: {data}", flush=True)

        # Zapisz w Redisie z kluczem np. username:action
        key = f"user:{data.get('userName')}"
        value = data.get('action')
        r.set(key, value)

        print(f" [*] Inserted into Redis: {key} -> {value}", flush=True)
        ch.basic_ack(delivery_tag=method.delivery_tag)

    except Exception as e:
        print(f" [!] Error: {e}", flush=True)

def main():
    credentials = pika.PlainCredentials('student', 'student')
    parameters = pika.ConnectionParameters('springproject-rabbitmq-1', 5672, '/', credentials)
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    channel.queue_declare(queue='action_log_queue', durable=False)

    channel.basic_consume(queue='action_log_queue', on_message_callback=callback, auto_ack=False)

    print(' [*] Waiting for messages...', flush=True)
    channel.start_consuming()

if __name__ == '__main__':
    main()
