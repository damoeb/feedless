-- AlterTable
ALTER TABLE `Article` ADD COLUMN `has_audio` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `has_video` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `length_audio` INTEGER,
    ADD COLUMN `length_video` INTEGER,
    ADD COLUMN `word_count_text` INTEGER;
