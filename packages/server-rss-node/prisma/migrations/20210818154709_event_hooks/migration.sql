-- CreateTable
CREATE TABLE `EventHook` (
    `id` VARCHAR(191) NOT NULL,
    `ownerId` VARCHAR(191) NOT NULL,
    `event` VARCHAR(191) NOT NULL,
    `scriptOrUrl` TEXT NOT NULL,
    `source_url` VARCHAR(500),

    UNIQUE INDEX `EventHook.source_url_unique`(`source_url`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `EventHook` ADD FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
