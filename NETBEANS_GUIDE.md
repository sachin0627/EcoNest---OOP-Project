# Apache NetBeans Setup & Demonstration Guide
## EcoNest – Smart Home & Garden Retail Management System

This guide explains how to open, build, run, and demonstrate the **EcoNest** project in **Apache NetBeans IDE**.

---

## 📌 Step 1: Open the Project in Apache NetBeans

1. Extract the `EcoNest-Retail-System.zip` file onto your computer (e.g., `Documents` or `Desktop`).
2. Launch **Apache NetBeans IDE**.
3. In NetBeans, go to the top menu:  
   **File ➔ Open Project...** (or press `Ctrl + Shift + O`).
4. Browse to the extracted folder: `econest`.  
   *(NetBeans will automatically recognize it as a project with a small coffee cup / Maven icon).*
5. Select the `econest` folder and click **Open Project**.

---

## 📌 Step 2: Ensure MySQL Server is Running

Before running the application:
1. Make sure your local **MySQL Server** (or XAMPP / WampServer MySQL) is started on port `3306`.
2. Ensure you have imported the database:
   - In MySQL Workbench, phpMyAdmin, or terminal:
   ```sql
   source /path/to/econest_db.sql
   ```
   *(The database name is `econest_db`. All 11 tables and realistic Sri Lankan seed data are included).*

---

## 📌 Step 3: Run the Application in NetBeans

1. Right-click on the **EcoNest Retail Management System** project in the left **Projects** tab.
2. Click **Run** (or simply press the keyboard shortcut **`F6`** or click the green **Play** button on the toolbar).
3. Apache NetBeans will compile and launch the **EcoNest Desktop Application** (`LoginFrame`).

> [!TIP]
> **Pre-configured NetBeans Action:**  
> The project includes `nbactions.xml`. NetBeans automatically knows that `com.econest.Main` is the main entry class, so you do not need to manually configure any run arguments or main class settings!

---

## 📌 Step 4: Login & Demonstration to Your Lecturer

When the application opens:
1. **Administrator Login (Full Access):**
   - **Username:** `admin`
   - **Password:** `admin123`
   - *(Or click the **"Use Admin"** quick-fill button)*
   - Demonstrates: Full access to all 9 modules, Settings, and User Management.

2. **Staff Login (Role-Based Access Control):**
   - **Username:** `staff`
   - **Password:** `staff123`
   - *(Or click the **"Use Staff"** quick-fill button)*
   - Demonstrates: Operational access; restricted from modifying system settings or user accounts.

---

## 📌 Step 5: How to Demonstrate the 9 Modules to Your Lecturer

1. **Module 01 – Product Catalogue:**
   - Go to **Product Catalogue** in the sidebar.
   - Filter by category ("Indoor Plants", "Planters", etc.).
   - Click **+ Add Product** or select a row and click **Edit** / **Delete**.

2. **Module 02 – Client Management:**
   - Go to **Client Management**.
   - Show Household and Office clients.
   - Select a client and click **Order History** to show all past orders placed by that client.

3. **Module 03 – Inventory Management:**
   - Go to **Inventory & Stock**.
   - Click the **"Show Low Stock Alerts Only"** button to highlight items at or below reorder levels.
   - Click **+ Stock-In from Supplier** to replenish stock; show the updated stock and movement history tab.

4. **Module 04 – Delivery Agents:**
   - Go to **Delivery Agents**.
   - Show drivers, vehicle types, and click **Toggle Availability** to switch agent status.

5. **Module 05 – Order Processing:**
   - Go to **Order Management**.
   - Click **+ Create Order**: Select a client (notice automatic address prefill and polymorphic tier discount!), add products, and click **Create & Save Order**.
   - Select the pending order and click **Confirm Order (Deduct Stock)**. Explain to your lecturer that this executes an **ACID database transaction** that verifies stock, deducts inventory, and logs stock movements.
   - Click **View Invoice / PDF** to display the branded commercial invoice and export it to PDF.

6. **Module 06 – Delivery Dispatch:**
   - Go to **Delivery Dispatch**.
   - Click **+ Assign Agent to Order** to schedule a confirmed order to an available driver.
   - Select a delivery and click **Update Delivery Progress** to transition status (e.g. to "In Transit").

7. **Module 07 – Reports & Analytics:**
   - Go to **Reports & Analytics**.
   - View the embedded **JFreeChart** monthly sales bar chart and product category pie chart.
   - Switch report types and click **Export Report PDF** or **Export CSV**.

8. **Module 08 & 09 – Email Notifications:**
   - Go to **Email Notifications** in the sidebar.
   - Show the audit log of dispatch emails automatically sent to clients and assignment alerts sent to delivery agents.

---

## 📌 Troubleshooting in NetBeans

- **"Cannot connect to MySQL"**: Check `.env` in the project root to ensure `DB_USER` and `DB_PASSWORD` match your MySQL credentials.
- **Offline / No Internet in Lab**: All required JAR libraries (`mysql-connector`, `flatlaf`, `jfreechart`, `itextpdf`, `jakarta.mail`) are also provided in the `lib/` directory inside the project.
