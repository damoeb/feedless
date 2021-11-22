-- AlterTable
ALTER TABLE `ArticleExporterTarget` ADD COLUMN `forward_errors` BOOLEAN NOT NULL DEFAULT false,
    MODIFY `context` VARCHAR(191);

-- AlterTable
ALTER TABLE `ArticlePostProcessor` MODIFY `context` VARCHAR(191);

-- AlterTable
ALTER TABLE `Feed` ADD COLUMN `harvest_prerender` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `harvest_site` BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN `retention_size` INTEGER;
