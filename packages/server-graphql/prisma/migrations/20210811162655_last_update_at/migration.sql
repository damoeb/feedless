/*
  Warnings:

  - You are about to drop the column `updatedAt` on the `Bucket` table. All the data in the column will be lost.
  - You are about to drop the column `updatedAt` on the `Feed` table. All the data in the column will be lost.
  - You are about to drop the column `updatedAt` on the `Subscription` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE `Bucket` DROP COLUMN `updatedAt`,
    ADD COLUMN `lastUpdatedAt` DATETIME(3);

-- AlterTable
ALTER TABLE `Feed` DROP COLUMN `updatedAt`,
    ADD COLUMN `lastUpdatedAt` DATETIME(3);

-- AlterTable
ALTER TABLE `Subscription` DROP COLUMN `updatedAt`,
    ADD COLUMN `lastUpdatedAt` DATETIME(3);
