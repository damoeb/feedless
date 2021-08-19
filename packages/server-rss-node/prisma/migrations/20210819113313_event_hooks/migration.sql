-- CreateTable
CREATE TABLE `EventHook` (
    `id` VARCHAR(191) NOT NULL,
    `ownerId` VARCHAR(191) NOT NULL,
    `event` VARCHAR(191) NOT NULL,
    `type` VARCHAR(191) NOT NULL,
    `script_or_url` TEXT NOT NULL,
    `script_source_url` VARCHAR(500),

    UNIQUE INDEX `EventHook.script_source_url_unique`(`script_source_url`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `EventHook` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
