CREATE  TABLE IF NOT EXISTS videos (
	id                   VARCHAR(26)  NOT NULL     PRIMARY KEY,
	unlocked_id          VARCHAR(26) ,
    unlocked_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 ) engine=InnoDB;
