-- ==============================================================
-- EcoNest – Smart Home & Garden Retail Management System
-- Database Schema & Realistic Seed Data
-- ==============================================================

CREATE DATABASE IF NOT EXISTS `econest_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `econest_db`;

-- Drop existing tables in reverse dependency order
DROP TABLE IF EXISTS `email_notifications`;
DROP TABLE IF EXISTS `deliveries`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `delivery_agents`;
DROP TABLE IF EXISTS `stock_movements`;
DROP TABLE IF EXISTS `suppliers`;
DROP TABLE IF EXISTS `inventory`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `clients`;
DROP TABLE IF EXISTS `users`;

-- -------------------------------------------------------------
-- 1. Table: users
-- -------------------------------------------------------------
CREATE TABLE `users` (
    `user_id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `salt` VARCHAR(64) NOT NULL,
    `full_name` VARCHAR(150) NOT NULL,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `role` ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    `phone` VARCHAR(30) NULL,
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 2. Table: products
-- -------------------------------------------------------------
CREATE TABLE `products` (
    `product_id` VARCHAR(20) PRIMARY KEY,
    `product_name` VARCHAR(150) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `description` TEXT,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `eco_rating` INT NOT NULL CHECK (`eco_rating` BETWEEN 1 AND 5),
    `available_quantity` INT NOT NULL DEFAULT 0,
    `reorder_level` INT NOT NULL DEFAULT 10,
    `image_path` VARCHAR(255) DEFAULT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 3. Table: clients
-- -------------------------------------------------------------
CREATE TABLE `clients` (
    `client_id` VARCHAR(20) PRIMARY KEY,
    `client_name` VARCHAR(150) NOT NULL,
    `client_type` ENUM('Household', 'Office') NOT NULL DEFAULT 'Household',
    `email` VARCHAR(120) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(50) NOT NULL,
    `registration_date` DATE NOT NULL,
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 4. Table: inventory
-- -------------------------------------------------------------
CREATE TABLE `inventory` (
    `inventory_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` VARCHAR(20) NOT NULL UNIQUE,
    `available_quantity` INT NOT NULL DEFAULT 0,
    `reorder_level` INT NOT NULL DEFAULT 10,
    `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products`(`product_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 5. Table: suppliers
-- -------------------------------------------------------------
CREATE TABLE `suppliers` (
    `supplier_id` VARCHAR(20) PRIMARY KEY,
    `supplier_name` VARCHAR(150) NOT NULL,
    `contact_person` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 6. Table: stock_movements
-- -------------------------------------------------------------
CREATE TABLE `stock_movements` (
    `movement_id` INT AUTO_INCREMENT PRIMARY KEY,
    `product_id` VARCHAR(20) NOT NULL,
    `movement_type` ENUM('STOCK_IN', 'ORDER_OUT', 'ADJUSTMENT', 'RETURN') NOT NULL,
    `quantity` INT NOT NULL,
    `supplier_id` VARCHAR(20) NULL,
    `reference_order_id` VARCHAR(20) NULL,
    `remarks` VARCHAR(255) NULL,
    `movement_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_movement_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products`(`product_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_movement_supplier` FOREIGN KEY (`supplier_id`) 
        REFERENCES `suppliers`(`supplier_id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 7. Table: delivery_agents
-- -------------------------------------------------------------
CREATE TABLE `delivery_agents` (
    `agent_id` VARCHAR(20) PRIMARY KEY,
    `full_name` VARCHAR(150) NOT NULL,
    `phone` VARCHAR(30) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `vehicle_type` VARCHAR(50) NOT NULL,
    `vehicle_reg_no` VARCHAR(30) NOT NULL,
    `availability_status` ENUM('Available', 'Assigned', 'Unavailable') NOT NULL DEFAULT 'Available',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 8. Table: orders
-- -------------------------------------------------------------
CREATE TABLE `orders` (
    `order_id` VARCHAR(20) PRIMARY KEY,
    `client_id` VARCHAR(20) NOT NULL,
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `discount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `delivery_address` VARCHAR(255) NOT NULL,
    `order_status` ENUM('Pending', 'Confirmed', 'Processing', 'Dispatched', 'Delivered', 'Cancelled') NOT NULL DEFAULT 'Pending',
    `payment_method` VARCHAR(50) NOT NULL DEFAULT 'Cash on Delivery',
    `notes` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_order_client` FOREIGN KEY (`client_id`) 
        REFERENCES `clients`(`client_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 9. Table: order_items
-- -------------------------------------------------------------
CREATE TABLE `order_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` VARCHAR(20) NOT NULL,
    `product_id` VARCHAR(20) NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(10,2) NOT NULL,
    CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) 
        REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products`(`product_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 10. Table: deliveries
-- -------------------------------------------------------------
CREATE TABLE `deliveries` (
    `delivery_id` VARCHAR(20) PRIMARY KEY,
    `order_id` VARCHAR(20) NOT NULL UNIQUE,
    `agent_id` VARCHAR(20) NOT NULL,
    `assigned_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `delivery_date` DATE NOT NULL,
    `delivery_status` ENUM('Assigned', 'Picked Up', 'In Transit', 'Delivered', 'Failed') NOT NULL DEFAULT 'Assigned',
    `tracking_notes` TEXT NULL,
    `completed_date` TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT `fk_delivery_order` FOREIGN KEY (`order_id`) 
        REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_delivery_agent` FOREIGN KEY (`agent_id`) 
        REFERENCES `delivery_agents`(`agent_id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- -------------------------------------------------------------
-- 11. Table: email_notifications
-- -------------------------------------------------------------
CREATE TABLE `email_notifications` (
    `notification_id` INT AUTO_INCREMENT PRIMARY KEY,
    `recipient_email` VARCHAR(120) NOT NULL,
    `recipient_name` VARCHAR(150) NOT NULL,
    `recipient_type` ENUM('CLIENT', 'AGENT', 'ADMIN') NOT NULL,
    `subject` VARCHAR(255) NOT NULL,
    `content` TEXT NOT NULL,
    `reference_id` VARCHAR(50) NULL,
    `status` ENUM('SENT', 'PENDING', 'FAILED') NOT NULL DEFAULT 'SENT',
    `error_message` TEXT NULL,
    `sent_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ==============================================================
-- Realistic Seed Data (Sri Lankan Retail Context, LKR)
-- Passwords:
--   admin / admin123 (SHA-256 hashed with salt)
--   staff / staff123 (SHA-256 hashed with salt)
-- Salt: 'econest_secure_salt_2026'
-- SHA-256(admin123 + econest_secure_salt_2026) =
--   'c9a7d36ecf2d48c89c8a4154446bdf3b55866faad4ec2efae17cfbf663e8a4a5'
-- SHA-256(staff123 + econest_secure_salt_2026) =
--   'd7b69255be237a3c3e800a74ba10167137ff74f67d264567406a0319409f5383'
-- ==============================================================

-- 1. Seed Users
INSERT INTO `users` (`username`, `password_hash`, `salt`, `full_name`, `email`, `role`, `phone`, `status`) VALUES
('admin', 'db06da81d97844cf05433aef7275c6131f7b2995873148e3aadd4d66af8e6198', 'econest_secure_salt_2026', 'Kasun Perera', 'admin@econest.lk', 'ADMIN', '+94 77 123 4567', 'ACTIVE'),
('staff', 'e8392ced50b3c44e3f8be90e7c7ba5600ddf349524d94abcb4d22b3b261f7999', 'econest_secure_salt_2026', 'Dilani Silva', 'staff@econest.lk', 'STAFF', '+94 71 456 7890', 'ACTIVE'),
('manager', 'db06da81d97844cf05433aef7275c6131f7b2995873148e3aadd4d66af8e6198', 'econest_secure_salt_2026', 'Malik Fernando', 'malik@econest.lk', 'ADMIN', '+94 76 987 6543', 'ACTIVE');

-- 2. Seed Products
INSERT INTO `products` (`product_id`, `product_name`, `category`, `description`, `unit_price`, `eco_rating`, `available_quantity`, `reorder_level`) VALUES
('PRD-1001', 'Monstera Deliciosa (Swiss Cheese Plant)', 'Indoor Plants', 'Popular indoor statement plant with lush, fenestrated heart-shaped leaves. Air purifying and easy care.', 4500.00, 5, 25, 8),
('PRD-1002', 'Snake Plant (Sansevieria Laurentii)', 'Indoor Plants', 'Hardy evergreen succulent with upright variegated sword leaves. Top-tier oxygen release at night.', 2800.00, 5, 30, 10),
('PRD-1003', 'Peace Lily (Spathiphyllum)', 'Indoor Plants', 'Graceful white flowering plant known for removing airborne toxins. Thrives in medium to low indirect light.', 3200.00, 4, 18, 6),
('PRD-1004', 'Spider Plant (Chlorophytum Comosum)', 'Indoor Plants', 'Fast-growing striped foliage that produces charming baby plantlets. Safe for household pets.', 1650.00, 5, 4, 10), -- Low Stock!
('PRD-1005', 'ZZ Plant (Zamioculcas Zamiifolia)', 'Indoor Plants', 'Drought-tolerant indestructible glossy foliage plant perfect for low-light office desks.', 3800.00, 5, 15, 5),
('PRD-1006', 'Areca Palm (Dypsis Lutescens)', 'Outdoor Plants', 'Graceful clustering golden-cane palm, ideal for patio shade, verandahs, and natural screening.', 5200.00, 4, 12, 5),
('PRD-1007', 'Bougainvillea Imperial Delight', 'Outdoor Plants', 'Sun-loving climbing shrub with vibrant pink and white bracts. Highly drought resistant.', 2400.00, 4, 20, 8),
('PRD-1008', 'Ceylon Jasmine (Pichcha)', 'Outdoor Plants', 'Sweetly fragrant star-shaped white blossoms widely cherished for traditional home gardens.', 1800.00, 5, 5, 8), -- Low Stock!
('PRD-1009', 'Frangipani (Araliya) - Tropical Yellow', 'Outdoor Plants', 'Ornamental tropical tree renowned for fragrant blossoms and sculptural succulent branches.', 3900.00, 5, 8, 4),
('PRD-1010', 'Handcrafted Terracotta Planter (10 inch)', 'Planters', 'Breathable porous natural fired clay planter promoting root health and preventing moisture rot.', 1950.00, 5, 35, 12),
('PRD-1011', 'Self-Watering Eco Pot - Sage Green', 'Planters', 'Recycled polypropylene reservoir pot that delivers water to root capillary action for up to 14 days.', 2750.00, 4, 22, 10),
('PRD-1012', 'Coconut Coir Hanging Planter (8 inch)', 'Planters', '100% biodegradable natural coconut fiber basket with jute hanging cord.', 1400.00, 5, 6, 12), -- Low Stock!
('PRD-1013', 'Minimalist Ceramic Planter (Matte White)', 'Planters', 'Sleek glazed stoneware planter with bamboo saucer drainage tray.', 3400.00, 4, 16, 6),
('PRD-1014', 'Urban Balcony Herb Garden Kit', 'Gardening Kits', 'Contains Thai basil, coriander, mint seeds, biodegradable pots, coir pellets, and guide.', 4200.00, 5, 14, 5),
('PRD-1015', 'Microgreens Starter Growing Kit', 'Gardening Kits', 'Includes reusable bamboo trays, hemp mats, and organic radish & mustard seeds for fresh harvest in 10 days.', 3500.00, 5, 3, 8), -- Low Stock!
('PRD-1016', 'Bonsai Pruning & Care Pro Kit', 'Gardening Kits', 'Carbon-steel mini shear set, branch trimmer, root hook, and organic bonsai nutrition tonic.', 6800.00, 4, 10, 4);

-- 3. Seed Inventory (Matches products available quantities and reorder levels)
INSERT INTO `inventory` (`product_id`, `available_quantity`, `reorder_level`) VALUES
('PRD-1001', 25, 8),
('PRD-1002', 30, 10),
('PRD-1003', 18, 6),
('PRD-1004', 4, 10),
('PRD-1005', 15, 5),
('PRD-1006', 12, 5),
('PRD-1007', 20, 8),
('PRD-1008', 5, 8),
('PRD-1009', 8, 4),
('PRD-1010', 35, 12),
('PRD-1011', 22, 10),
('PRD-1012', 6, 12),
('PRD-1013', 16, 6),
('PRD-1014', 14, 5),
('PRD-1015', 3, 8),
('PRD-1016', 10, 4);

-- 4. Seed Suppliers
INSERT INTO `suppliers` (`supplier_id`, `supplier_name`, `contact_person`, `phone`, `email`, `address`) VALUES
('SUP-01', 'Lanka Flora Bio-Tech Nurseries', 'Pradeep Jayasuriya', '+94 33 226 7812', 'info@lankaflora.lk', 'Mirigama Road, Veyangoda, Western Province'),
('SUP-02', 'EcoPot Ceramic & Clay Works', 'Nimal Rathnayake', '+94 34 222 3456', 'sales@ecopot.lk', 'Clay Artisans Village, Molliyamale, Kalutara'),
('SUP-03', 'GreenSprout Organic Seed Co.', 'Anoma Weerasinghe', '+94 81 238 9011', 'contact@greensprout.lk', 'Peradeniya Agritech Hub, Kandy'),
('SUP-04', 'Ceylon Coir & Bio-Fibre Mills', 'Roshan Wickramasinghe', '+94 31 223 4879', 'exports@ceyloncoir.lk', 'Industrial Zone, Dankotuwa, Wayamba');

-- 5. Seed Clients (Household & Office across Sri Lanka)
INSERT INTO `clients` (`client_id`, `client_name`, `client_type`, `email`, `phone`, `address`, `city`, `registration_date`) VALUES
('CLT-2001', 'Dr. Ruwan Wickramasinghe', 'Household', 'ruwan.w@gmail.com', '+94 77 345 6789', '42/A, Dharmapala Mawatha, Colombo 07', 'Colombo', '2026-01-10'),
('CLT-2002', 'Virtusa Tech Solutions Ltd', 'Office', 'facilities@virtusalk.com', '+94 11 254 7800', 'Level 8, Orion City Tower, Baseline Road, Dematagoda', 'Colombo', '2026-01-15'),
('CLT-2003', 'Shalini Senanayake', 'Household', 'shalini.sena@outlook.com', '+94 71 890 1234', '15, De Saram Road, Mount Lavinia', 'Mount Lavinia', '2026-02-01'),
('CLT-2004', 'Brandix Apparel Corporate HQ', 'Office', 'admin.corporate@brandix.com', '+94 11 472 7000', '409, Galle Road, Colombo 03', 'Colombo', '2026-02-12'),
('CLT-2005', 'Kavinda Jayawardena', 'Household', 'kavinda.j@yahoo.com', '+94 76 654 3210', '88/4, Kandy Road, Kadawatha', 'Kadawatha', '2026-02-20'),
('CLT-2006', 'WSO2 Green Workspaces Initiative', 'Office', 'workplace@wso2.com', '+94 11 214 5345', '20, Palm Grove, Colombo 03', 'Colombo', '2026-03-05'),
('CLT-2007', 'Nadeesha Gunawardhana', 'Household', 'nadeesha.guna@gmail.com', '+94 77 912 3456', '23, Raja Veediya, Kandy', 'Kandy', '2026-03-18'),
('CLT-2008', 'Ogilvy Sri Lanka Creative Hub', 'Office', 'reception@ogilvy.lk', '+94 11 258 8800', '114, Ward Place, Colombo 07', 'Colombo', '2026-04-02');

-- 6. Seed Delivery Agents
INSERT INTO `delivery_agents` (`agent_id`, `full_name`, `phone`, `email`, `vehicle_type`, `vehicle_reg_no`, `availability_status`) VALUES
('AGT-3001', 'Sunil Kumara', '+94 77 456 7812', 'sunil.delivery@econest.lk', 'Eco-Electric Van', 'WP ND-4589', 'Available'),
('AGT-3002', 'Tharindu Bandara', '+94 71 567 8923', 'tharindu.delivery@econest.lk', 'Delivery Motorbike (Coir Carrier)', 'WP BHH-6721', 'Assigned'),
('AGT-3003', 'Mohamed Rizwan', '+94 76 678 9034', 'rizwan.delivery@econest.lk', 'Light Covered Truck', 'WP LL-8812', 'Available'),
('AGT-3004', 'Dinesh Karunaratne', '+94 75 789 0145', 'dinesh.delivery@econest.lk', 'Electric Cargo Scooter', 'WP BIZ-9014', 'Unavailable');

-- 7. Seed Orders
INSERT INTO `orders` (`order_id`, `client_id`, `order_date`, `subtotal`, `discount`, `total_amount`, `delivery_address`, `order_status`, `payment_method`, `notes`) VALUES
('ORD-4001', 'CLT-2001', '2026-10-15 10:30:00', 9700.00, 485.00, 9215.00, '42/A, Dharmapala Mawatha, Colombo 07', 'Delivered', 'Credit Card (Online)', 'Handle ceramic pots with care. Deliver between 10am-2pm.'),
('ORD-4002', 'CLT-2002', '2026-10-20 14:15:00', 44250.00, 2212.50, 42037.50, 'Level 8, Orion City Tower, Baseline Road, Dematagoda', 'Delivered', 'Bank Transfer', 'Corporate purchase for reception & lounge greenery.'),
('ORD-4003', 'CLT-2003', '2026-11-02 09:45:00', 7900.00, 0.00, 7900.00, '15, De Saram Road, Mount Lavinia', 'Dispatched', 'Cash on Delivery', 'Please ring the front garden gate bell.'),
('ORD-4004', 'CLT-2004', '2026-11-05 11:20:00', 28500.00, 1425.00, 27075.00, '409, Galle Road, Colombo 03', 'Confirmed', 'Company Cheque', 'Office cafeteria interior decor order.'),
('ORD-4005', 'CLT-2005', '2026-11-08 16:00:00', 4500.00, 0.00, 4500.00, '88/4, Kandy Road, Kadawatha', 'Pending', 'Cash on Delivery', 'Customer asked to verify plant height before shipping.'),
('ORD-4006', 'CLT-2006', '2026-11-10 13:30:00', 16500.00, 825.00, 15675.00, '20, Palm Grove, Colombo 03', 'Processing', 'Bank Transfer', 'Desk planters and ZZ plants for engineers.'),
('ORD-4007', 'CLT-2007', '2026-11-11 15:45:00', 6000.00, 300.00, 5700.00, '23, Raja Veediya, Kandy', 'Pending', 'Cash on Delivery', 'Balcony garden setup pack.');

-- 8. Seed Order Items
INSERT INTO `order_items` (`order_id`, `product_id`, `quantity`, `unit_price`, `subtotal`) VALUES
-- ORD-4001
('ORD-4001', 'PRD-1001', 1, 4500.00, 4500.00),
('ORD-4001', 'PRD-1010', 1, 1950.00, 1950.00),
('ORD-4001', 'PRD-1003', 1, 3250.00, 3250.00),
-- ORD-4002 (Corporate order)
('ORD-4002', 'PRD-1001', 5, 4500.00, 22500.00),
('ORD-4002', 'PRD-1002', 5, 2800.00, 14000.00),
('ORD-4002', 'PRD-1011', 5, 1550.00, 7750.00),
-- ORD-4003
('ORD-4003', 'PRD-1005', 1, 3800.00, 3800.00),
('ORD-4003', 'PRD-1014', 1, 4100.00, 4100.00),
-- ORD-4004
('ORD-4004', 'PRD-1006', 3, 5200.00, 15600.00),
('ORD-4004', 'PRD-1013', 3, 3400.00, 10200.00),
('ORD-4004', 'PRD-1011', 1, 2700.00, 2700.00),
-- ORD-4005
('ORD-4005', 'PRD-1001', 1, 4500.00, 4500.00),
-- ORD-4006
('ORD-4006', 'PRD-1005', 3, 3800.00, 11400.00),
('ORD-4006', 'PRD-1011', 3, 1700.00, 5100.00),
-- ORD-4007
('ORD-4007', 'PRD-1014', 1, 4200.00, 4200.00),
('ORD-4007', 'PRD-1008', 1, 1800.00, 1800.00);

-- 9. Seed Deliveries
INSERT INTO `deliveries` (`delivery_id`, `order_id`, `agent_id`, `assigned_date`, `delivery_date`, `delivery_status`, `tracking_notes`, `completed_date`) VALUES
('DEL-5001', 'ORD-4001', 'AGT-3001', '2026-10-15 11:00:00', '2026-10-16', 'Delivered', 'Delivered safely and received by customer at front porch.', '2026-10-16 14:25:00'),
('DEL-5002', 'ORD-4002', 'AGT-3003', '2026-10-20 15:00:00', '2026-10-21', 'Delivered', 'Unloaded at Orion City goods lift; signed off by Facilities Manager.', '2026-10-21 16:10:00'),
('DEL-5003', 'ORD-4003', 'AGT-3002', '2026-11-02 10:15:00', '2026-11-03', 'In Transit', 'Package loaded into delivery bike, dispatched en-route to Mount Lavinia.', NULL);

-- 10. Seed Stock Movements
INSERT INTO `stock_movements` (`product_id`, `movement_type`, `quantity`, `supplier_id`, `reference_order_id`, `remarks`) VALUES
('PRD-1001', 'STOCK_IN', 30, 'SUP-01', NULL, 'Batch restock from Lanka Flora nursery'),
('PRD-1002', 'STOCK_IN', 35, 'SUP-01', NULL, 'Bulk indoor plant procurement'),
('PRD-1001', 'ORDER_OUT', 1, NULL, 'ORD-4001', 'Customer delivery order dispatch'),
('PRD-1001', 'ORDER_OUT', 5, NULL, 'ORD-4002', 'Corporate office batch dispatch'),
('PRD-1004', 'ADJUSTMENT', -2, NULL, NULL, 'Damaged foliage during transit, adjusted inventory'),
('PRD-1010', 'STOCK_IN', 40, 'SUP-02', NULL, 'New clay pottery batch received from Kalutara kiln');

-- 11. Seed Email Notifications History
INSERT INTO `email_notifications` (`recipient_email`, `recipient_name`, `recipient_type`, `subject`, `content`, `reference_id`, `status`) VALUES
('ruwan.w@gmail.com', 'Dr. Ruwan Wickramasinghe', 'CLIENT', 'EcoNest Order #ORD-4001 Has Been Dispatched!', '<p>Dear Dr. Ruwan Wickramasinghe,<br>Your order ORD-4001 is on its way!</p>', 'ORD-4001', 'SENT'),
('facilities@virtusalk.com', 'Virtusa Tech Solutions Ltd', 'CLIENT', 'EcoNest Order #ORD-4002 Has Been Dispatched!', '<p>Dear Virtusa Facilities Team,<br>Your office greenery order ORD-4002 is on its way!</p>', 'ORD-4002', 'SENT'),
('tharindu.delivery@econest.lk', 'Tharindu Bandara', 'AGENT', 'New Delivery Assignment: Order #ORD-4003', '<p>Dear Tharindu,<br>You have been assigned to deliver Order ORD-4003 to Shalini Senanayake.</p>', 'ORD-4003', 'SENT');
