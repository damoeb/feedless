-- AlterTable
ALTER TABLE `Article` MODIFY `source_url` VARCHAR(500);

-- AlterTable
ALTER TABLE `Feed` MODIFY `feed_url` VARCHAR(500) NOT NULL;
