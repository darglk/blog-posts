CREATE TABLE posts (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    content TEXT NOT NULL,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id),
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT current_timestamp(3)
);

CREATE TABLE post_upvotes (
    post_id VARCHAR(36) NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE post_comments (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
    content TEXT NOT NULL,
    created_at TIMESTAMP(3) WITH TIME ZONE NOT NULL DEFAULT current_timestamp(3)
);

CREATE TABLE comment_upvotes (
    comment_id VARCHAR(36) NOT NULL REFERENCES post_comments (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE tags (
    name VARCHAR(100) NOT NULL PRIMARY KEY UNIQUE
);

CREATE TABLE tags_blacklists (
    tag_name VARCHAR(100) NOT NULL REFERENCES tags(name) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE tags_favorites (
    tag_name VARCHAR(100) NOT NULL REFERENCES tags(name) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE post_favorites (
    post_id VARCHAR(36) NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE comment_favorites (
    comment_id VARCHAR(36) NOT NULL REFERENCES comments (id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE users_favorites (
    user_id VARCHAR(36) NOT NULL REFERENCES users (id),
    favorite_user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE users_blacklist (
    user_id VARCHAR(36) NOT NULL REFERENCES users (id),
    blocked_by_user_id VARCHAR(36) NOT NULL REFERENCES users (id)
);

CREATE TABLE post_attachments (
    post_id VARCHAR(36) NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    url VARCHAR(512) NOT NULL
);

CREATE TABLE comment_attachments (
    comment_id VARCHAR(36) NOT NULL REFERENCES comments (id) ON DELETE CASCADE,
    url VARCHAR(512) NOT NULL
);

CREATE TABLE post_tags (
    tag_name VARCHAR(100) NOT NULL REFERENCES tags(name) ON DELETE CASCADE,
    post_id VARCHAR(36) NOT NULL REFERENCES posts (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX post_upvotes_idx ON post_upvotes (user_id, post_id);
CREATE UNIQUE INDEX comment_upvotes_idx ON post_upvotes (user_id, comment_id);
CREATE UNIQUE INDEX post_tags_idx ON post_tags (post_id, tag_name);
CREATE UNIQUE INDEX tags_blacklist_idx ON tags_blacklist (user_id, tag_name);
CREATE UNIQUE INDEX tags_favorites_idx ON tags_favorites (user_id, tag_name);
CREATE UNIQUE INDEX post_attachments_idx ON post_attachments (post_id, url);
CREATE UNIQUE INDEX comment_attachments_idx ON comment_attachments (comment_id, url);
CREATE UNIQUE INDEX post_favorites_idx ON post_favorites (post_id, user_id);
CREATE UNIQUE INDEX comment_favorites_idx ON comment_favorites (comment_id, user_id);
CREATE UNIQUE INDEX users_favorites_idx ON users_favorites (user_id, favorite_user_id);
CREATE UNIQUE INDEX users_blacklist_idx ON users_blacklist (user_id, blocked_by_user_id);
