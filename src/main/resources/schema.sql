-- 채팅방
CREATE TABLE chat_room (
   chat_room_id SERIAL PRIMARY KEY,
   name VARCHAR(100) NOT NULL,
   create_dtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 채팅 메시지
CREATE TABLE chat_message (
   chat_message_id SERIAL PRIMARY KEY,
   chat_room_id INT NOT NULL REFERENCES chat_room(chat_room_id) ON DELETE CASCADE,
   sender_nickname VARCHAR(100) NOT NULL,
   content TEXT NOT NULL,
   message_type VARCHAR(10) DEFAULT 'CHAT' NOT NULL,
   send_dtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 채팅방 접속 게스트
CREATE TABLE chat_room_guest (
   chat_room_guest_id SERIAL PRIMARY KEY,
   nick_name VARCHAR(100) NOT NULL,
   chat_room_id INT NOT NULL REFERENCES chat_room(chat_room_id) ON DELETE CASCADE,
   joined_dtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   last_ping_dtime TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 채팅메시지 인덱스
CREATE INDEX idx_chat_room_id_send_dtime
ON chat_message(chat_room_id, send_dtime DESC);