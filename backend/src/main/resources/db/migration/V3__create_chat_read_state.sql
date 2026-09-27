CREATE TABLE chat_read_state (
    ride_id BIGINT NOT NULL REFERENCES rides(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    last_read_message_id BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (ride_id, user_id)
);

CREATE INDEX idx_chat_read_state_user_id
    ON chat_read_state(user_id);
