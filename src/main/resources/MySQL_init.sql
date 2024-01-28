CREATE  TABLE IF NOT EXISTS videos (
	id                   VARCHAR(26)  NOT NULL     PRIMARY KEY,
	unlocked_id          VARCHAR(26) ,
    unlocked_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    private              BOOLEAN DEFAULT FALSE
 ) engine=InnoDB;

CREATE  TABLE IF NOT EXISTS tiktok_videos (
	id                   VARCHAR(128)  NOT NULL     PRIMARY KEY,
	vk_id                VARCHAR(26) NOT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 ) engine=InnoDB;
