package com.example.ui.screens

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "en" }

object TranslationHelper {
    private val tamilMap = mapOf(
        // Navigation / Tabs
        "Dashboard" to "முகப்பு",
        "Sales" to "விற்பனை",
        "Bills" to "பில்கள்",
        "Ledger Bills & Invoices" to "பதிவேடு பில்கள் & விலைப்பட்டியல்கள்",
        "Reports" to "அறிக்கைகள்",
        "Settings" to "அமைப்புகள்",
        "Inventory" to "இருப்பு விவரம்",
        "ERP Vendor Console" to "ERP விற்பனையாளர் முனையம்",
        "Syncing" to "ஒத்திசைக்கப்படுகிறது...",
        "🟢 Synced" to "🟢 ஒத்திசைக்கப்பட்டது",
        "🔴 Unsynced" to "🔴 ஒத்திசைக்கப்படாதது",
        
        // Splash / Auth Screen
        "Dairy ERP" to "டெய்ரி ERP",
        "Manage Milk Sales Anywhere Offline First" to "பால் விற்பனையை எங்கும் ஆஃப்லைனில் நிர்வகிக்கவும்",
        "Login" to "உள்நுழை",
        "Register Business" to "வணிகத்தை பதிவுசெய்",
        "Back to Ledger" to "பேரேட்டிற்குத் திரும்பு",
        "Enter login credentials provided" to "உள்நுழைவு விவரங்களை உள்ளிடவும்",
        "Email Address" to "மின்னஞ்சல் முகவரி",
        "Primary Email Address" to "மின்னஞ்சல் முகவரி",
        "Password" to "கடவுச்சொல்",
        "Sign In" to "உள்நுழைக",
        "New Seller? Register Business Instead" to "புதிய விற்பனையாளரா? வணிகத்தை பதிவுசெய்யவும்",
        "Register ERP Account" to "வணிக கணக்கை பதிவுசெய்",
        "Create seller profile to work completely offline" to "ஆஃப்லைனில் வேலை செய்ய சுயவிவரத்தை உருவாக்கவும்",
        "Cooperative Business Name" to "கூட்டுறவு வணிகப் பெயர்",
        "Owner Full Name" to "உரிமையாளரின் முழுப் பெயர்",
        "Logistics Phone Contact" to "தொலைபேசி எண்",
        "ERP Security Password" to "ERP பாதுகாப்பு கடவுச்சொல்",
        "Mobile Phone Number" to "தொலைபேசி எண்",
        "Secret Password" to "ERP பாதுகாப்பு கடவுச்சொல்",
        "Business Name" to "கூட்டுறவு வணிகப் பெயர்",
        "Already registered? Login" to "ஏற்கனவே பதிவுசெய்யப்பட்டதா? உள்நுழைக",
        "Create Account & Enter" to "கணக்கை உருவாக்கி நுழையவும்",
        "You have logged in with the sample account details.\n\nTo continue using DairySync securely and protect your database logs, please change your business name, owner name, and password in settings screen immediately." to "நீங்கள் மாதிரி கணக்கு விவரங்களுடன் உள்நுழைந்துள்ளீர்கள்.\n\nடைரிசின்க்-ஐ பாதுகாப்பாகப் பயன்படுத்தவும் உங்கள் தரவுத்தளப் பதிவுகளைப் பாதுகாக்கவும், தயவுசெய்து அமைப்புகள் திரையில் உங்கள் வணிகப் பெயர், உரிமையாளர் பெயர் மற்றும் கடவுச்சொல்லை உடனடியாக மாற்றவும்.",
        "Go to Settings" to "அமைப்புகளுக்குச் செல்",
        "Remind Me Later" to "பின்னர் நினைவூட்டு",
        "Auto-Customer (%s...)" to "தானியங்கி வாடிக்கையாளர் (%s...)",
        "New Client Roll" to "புதிய வாடிக்கையாளர் பட்டியல்",
        "Immediate Client Sign-Up" to "உடனடி வாடிக்கையாளர் பதிவு",
        "Business or Customer Name" to "வணிகம் அல்லது வாடிக்கையாளர் பெயர்",
        "Logistics Phone (Optional)" to "தொலைபேசி எண் (விருப்பத்தேர்வு)",
        "Register & Use" to "பதிவுசெய்து பயன்படுத்து",
        "Please enter customer name!" to "தயவுசெய்து வாடிக்கையாளர் பெயரை உள்ளிடவும்!",
        "%s registered & selected!" to "%s பதிவு செய்யப்பட்டு தேர்ந்தெடுக்கப்பட்டது!",
        "Apply Custom Rate (₹ per Liter)" to "தனிப்பயன் விலையைப் பயன்படுத்து (லிட்டருக்கு ₹)",
        "Search by buyer name..." to "வாங்குபவர் பெயரில் தேடவும்...",
        "Encrypted local SQLite backup successfully written to app directory!" to "மறைகுறியாக்கப்பட்ட உள்ளூர் SQLite காப்புப்பிரதி வெற்றிகரமாக சேமிக்கப்பட்டது!",
        "Cloud backup restoration check: Up to date!" to "கிளவுட் காப்புப்பிரதி சரிபார்ப்பு: புதுப்பித்த நிலையில் உள்ளது!",
        "Buyer Name (e.g., Arun Sharma)" to "வாங்குபவர் பெயர் (எ.கா. அருண் சர்மா)",
        "Logistics Phone Number" to "தொலைபேசி எண்",
        "Register Profile" to "சுயவிவரத்தை பதிவுசெய்",
        "Base Price (₹/L)" to "அடிப்படை விலை (₹/லி)",
        "Update Base Class Rate" to "அடிப்படை விலையை மாற்றியமை",
        "Configure Inventory & Price Catalog" to "இருப்பு & விலை பட்டியலை அமை",
        "Set collections capability (liters) and active dynamic prices." to "கொள்முதல் அளவு (லிட்டர்) மற்றும் தற்போதைய விலைகளை அமைக்கவும்.",
        "Log Date (YYYY-MM-DD)" to "பதிவு தேதி (YYYY-MM-DD)",
        "Stock (L)" to "இருப்பு (லி)",
        "e.g. 100" to "எ.கா. 100",
        "Price (₹/L)" to "விலை (₹/லி)",
        "e.g. 50" to "எ.கா. 50",
        "Category Name (e.g. Goat Milk)" to "வகை பெயர் (எ.கா. ஆட்டுப் பால்)",
        "Baseline Rate per Liter (₹)" to "லிட்டருக்கான அடிப்படை விலை (₹)",
        "Logistics Contact Number" to "தொலைபேசி எண்",
        "Delivery Address / Client Base" to "விநியோக முகவரி",
        "Operational Notes / Preferences" to "குறிப்புகள் / விருப்பங்கள்",
        
        // Alerts & Prompts
        "Default Credentials Active" to "இயல்புநிலை விவரங்கள் செயலில்",
        "Tap here to change name/password in Settings to secure your ERP logs info." to "உங்கள் ERP தகவல்களைப் பாதுகாக்க அமைப்புகளில் பெயர்/கடவுச்சொல்லை மாற்ற இங்கே தட்டவும்.",
        "Change Account Details" to "கணக்கு விவரங்களை மாற்றவும்",
        "Today's Ledger" to "இன்றைய பதிவேடு",
        "Total Entries: %s" to "மொத்த பதிவுகள்: %s",
        "No sales recorded today." to "இன்று விற்பனை எதுவும் பதிவு செய்யப்படவில்லை.",
        "Quick ERP Actions" to "விரைவான ERP செயல்கள்",
        "New Sale" to "புதிய விற்பனை",
        "Add Customer" to "வாடிக்கையாளரைச் சேர்",
        "Register Customer Profile Ledger" to "வாடிக்கையாளர் சுயவிவர பதிவேடு",
        "Customer/Retail Outlet Name" to "வாடிக்கையாளர்/சில்லறை விற்பனை பெயர்",
        "Preferred QR Sync method" to "விருப்பமான QR ஒத்திசைவு முறை",
        "Add New Customer" to "புதிய வாடிக்கையாளரைச் சேர்",
        "Milk Sale Saved Successfully! (Synced)" to "பால் விற்பனை வெற்றிகரமாக சேமிக்கப்பட்டது! (ஒத்திசைக்கப்பட்டது)",
        "Yes" to "ஆம்",
        "No" to "இல்லை",
        "Confirm Sale" to "விற்பனையை உறுதிப்படுத்து",
        "Confirm Customer" to "வாடிக்கையாளரை உறுதிப்படுத்து",
        "Are you sure you want to save this milk sale?" to "இந்த பால் விற்பனையைச் சேமிக்க விரும்புகிறீர்களா?",
        "Are you sure you want to add this customer?" to "இந்த வாடிக்கையாளரைச் சேர்க்க விரும்புகிறீர்களா?",
        "Permission denied" to "அனுமதி மறுக்கப்பட்டது",
        "Location is required" to "இருப்பிடம் தேவை",
        "BANK" to "வங்கி",
        "Collect Cash" to "பணம் வசூலிக்கவும்",
        "Enterprise Reports" to "வணிக அறிக்கைகள்",
        "Real-time business analytics based on active database registers." to "செயலில் உள்ள தரவுத் தளங்களின் அடிப்படையில் நிகழ்நேர வணிகப் பகுப்பாய்வு.",
        
        // Dashboard Widgets
        "Welcome back," to "மீண்டும் வருக,",
        "Pending Payments" to "நிலுவையில் உள்ள தொகைகள்",
        "Recent Sales Pulse" to "சமீபத்திய விற்பனை",
        "View All Buyers" to "வாங்குபவர்களைப் பார்",
        "Record Rapid Sales" to "விற்பனையை விரைவாகப் பதிவுசெய்",
        "Total Pending" to "மொத்த நிலுவை",
        "Total Collected" to "மொத்த வசூல்",
        "Total Liters" to "மொத்த லிட்டர்கள்",
        "Total Revenue" to "மொத்த வருவாய்",
        "Liters" to "லிட்டர்கள்",
        "Rate" to "விலை",
        "Amount" to "தொகை",
        "Date" to "தேதி",
        "Status" to "நிலை",
        "Action" to "செயல்",
        
        // Sales Tab
        "Quick Procurement Entries" to "விரைவு பால் கொள்முதல் பதிவுகள்",
        "Quantity (Liters)" to "அளவு (லிட்டர்கள்)",
        "Rate per Liter" to "லிட்டருக்கான விலை",
        "Select Milk Type" to "பால் வகையைத் தேர்ந்தெடு",
        "Select Customer" to "வாடிக்கையாளரைத் தேர்ந்தெடு",
        "Payment Method" to "கட்டண முறை",
        "CASH" to "பணம் (CASH)",
        "UPI" to "UPI",
        "PENDING" to "நிலுவையில் உள்ளவை",
        "Save Milk Sale" to "பால் விற்பனையை சேமிக்கவும்",
        "Add New Customer Profile" to "புதிய வாடிக்கையாளர் சேர்க்கை",
        "Customer Name" to "வாடிக்கையாளர் பெயர்",
        "Phone Number" to "தொலைபேசி எண்",
        "Preferred QR Sync" to "முன்னுரிமை QR தேர்வு",
        "Dairy Sync" to "டெயிரிசின்க்",
        
        // Invoice / Receipt Dialog / Bills Tab
        "Invoice Details" to "இன்வாய்ஸ் விவரங்கள்",
        "Print Receipt" to "பிரிண்ட் ரசீது",
        "Mark as Paid" to "கட்டணம் செலுத்தப்பட்டது என குறி",
        "Export CSV" to "CSV ஏற்றுமதி",
        "Invoice for %s" to "%s-க்கான பில் ரசீது",
        "Date Issued:" to "வழங்கப்பட்ட தேதி:",
        "Status:" to "நிலை:",
        "Milk Details:" to "பால் விவரங்கள்:",
        "Type" to "வகை",
        "Total Amount" to "மொத்த தொகை",
        "Collected via" to "சேகரிக்கப்பட்ட முறை",
        "Pending Balance" to "நிலுவைத் தொகை",
        "Record Payment Delivery" to "பணப் பட்டுவாடாவை பதிவுசெய்க",
        "Settle via Digital UPI" to "டிஜிட்டல் UPI மூலம் செலுத்துக",
        "Settle via Liquid Cash" to "பணமாகச் செலுத்துக",
        "Print PDF Receipt" to "PDF ரசீது அச்சிடுக",
        "Share with Buyer" to "வாங்குபவருடன் பகிர்",
        "Dismiss" to "விலக்கு",
        "Search Bills" to "பில்களைத் தேடு",
        "Filter by Date" to "தேதி வாரியாக வடிகட்டு",
        "Today" to "இன்று",
        "Week" to "வாரம்",
        "Month" to "மாதம்",
        "Year" to "வருடம்",
        
        // Customer Profile Tab
        "Customer Ledgers" to "வாடிக்கையாளர் கணக்குப் புத்தகங்கள்",
        "Manage accounts, outstanding dues & deliveries" to "கணக்குகள் மற்றும் நிலுவைகளை நிர்வகிக்கவும்",
        "Search customer accounts..." to "வாடிக்கையாளர் கணக்குகளைத் தேடுங்கள்...",
        "Customer / Retail Outlet Name" to "வாடிக்கையாளர் / கடை பெயர்",
        "Notes" to "குறிப்புகள்",
        "Save Details" to "விவரங்களைச் சேமி",
        "Delete Customer" to "வாடிக்கையாளரை நீக்கு",
        "Settle Payment" to "கட்டணத்தைச் செலுத்து",
        "Back to List" to "பட்டியலுக்குத் திரும்பு",
        "Address" to "முகவரி",
        "Customer Details" to "வாடிக்கையாளர் விவரங்கள்",
        
        // Reports Tab
        "Business Analytics" to "வணிக பகுப்பாய்வு",
        "Average Price" to "சராசரி விலை",
        "Cow Milk" to "பசுவின் பால்",
        "Buffalo Milk" to "எருமையின் பால்",
        "A2 Milk" to "A2 பால்",
        "Paid" to "கட்டணம் செலுத்தப்பட்டது",
        "Pending" to "நிலுவையில் உள்ளது",
        
        // Settings / Theme Selection
        "License & System Integrity" to "உரிமம் மற்றும் கணினி மேலாண்மை",
        "License Status" to "உரிமம் நிலை",
        "PREMIUM LIFETIME" to "பிரீமியம் வாழ்நாள்",
        "FREE TRIAL MODE" to "இலவச சோதனை முறை",
        "TRIAL EXPIRED" to "சோதனை முடிந்தது",
        "Remaining Days" to "மீதமுள்ள நாட்கள்",
        "Remaining Days: %s days left (from total 31-day trial limit). All capabilities will auto lock after 31 days unless activated." to "மீதமுள்ள நாட்கள்: %s நாட்கள் (31-நாள் சோதனையில்). இயக்கப்படாவிட்டால் அனைத்து செயல்பாடுகளும் தானாகவே பூட்டப்படும்.",
        "Upgrade to Lifetime License (₹1,499)" to "வாழ்நாள் உரிமத்திற்கு மேம்படுத்தவும் (₹1,499)",
        "Your application is fully licensed for life! This offline terminal database is unlocked, protected, and secure." to "உங்கள் பயன்பாடு வாழ்நாள் முழுவதும் முழு உரிமம் பெற்றது! இந்த பாதுகாப்பான ஆஃப்லைத் தரவுத்தளம் திறக்கப்பட்டு பாதுகாக்கப்பட்டுள்ளது.",
        "Demo Verification Controls:" to "டெமோ சரிபார்ப்புக் கட்டுப்பாடுகள்:",
        "Reset 31-Day Trial" to "31-நாள் சோதனையை மீட்டமை",
        "Simulate Expiry" to "காலாவதியை உருவகப்படுத்து",
        "Business ERP Profile Settings" to "வணிக ERP சுயவிவர அமைப்புகள்",
        "Update Profile" to "சுயவிவரத்தை மாற்றுக",
        "Register Customer Profile Ledger" to "வாடிக்கையாளர் சுயவிவர பதிவேடு",
        "Preferred QR Sync method:" to "முன்னுரிமை QR ஒத்திசைவு முறை:",
        "Add New Customer" to "புதிய வாடிக்கையாளரைச் சேர்",
        "Registered buyers list:" to "பதிவுசெய்யப்பட்ட வாங்குபவர்கள் பட்டியல்:",
        "Cooperative System & Premium Tier" to "கூட்டுறவு அமைப்பு & பிரீமியம் அடுக்கு",
        "Premium Community Owner Dashboard" to "பிரீமியம் சமூக உரிமையாளர் முகப்பு",
        "Syncs other verified seller sheets and performs comparison analysis graphs." to "மற்ற சரிபார்க்கப்பட்ட விற்பனையாளர்களின் தாள்களை ஒத்திசைத்து ஒப்பீட்டு வரைபடங்களை உருவாக்குகிறது.",
        "Interface Appearance" to "பயன்பாட்டுத் தோற்றம்",
        "Enable Light Theme" to "ஒளிர் கருப்பொருளை இயக்கு",
        "Switch between White Theme (Light) and Dark Theme (Midnight)" to "ஒளிர் (வெள்ளை) மற்றும் இருண்ட கருப்பொருள்களுக்கு இடையே மாறவும்",
        "Milk Stock Inventory" to "பால் இருப்பு மேலாண்மை",
        "Manage Daily Milk Inventory" to "தினசரி பால் இருப்பை நிர்வகி",
        "Logged: Add today's milk volume for Cow, Buffalo & A2 Milk" to "பதிவுசெய்யப்பட்டது: பசு, எருமை மற்றும் A2 பாலின் இன்றைய அளவைச் சேர்க்கவும்",
        "Local Backups & Subscriptions" to "உள்ளூர் காப்புப்பிரதி & சந்தாக்கள்",
        "Local Backup" to "உள்ளூர் காப்புப்பிரதி",
        "Cloud Restore" to "கிளவுட் மீட்டமைப்பு (Cloud Restore)",
        "Logout from ERP Console" to "ERP கன்சோலில் இருந்து வெளியேறு",
        "Powered by" to "இயக்குவது",
        "abielan Tech." to "அபியலன் டெக்.",
        "www.abielan.in" to "www.abielan.in",
        
        // Inventory Screen
        "Daily Milk Stock Diary" to "தினசரி பால் இருப்பு மேலாண்மை",
        "Back to Settings" to "அமைப்புகளுக்குத் திரும்பு",
        "Manage Milk Categories & Pricing" to "பால் வகைகள் & விலையிடலை நிர்வகி",
        "Milk Type Category" to "பால் வகை",
        "Base Rate per Liter (₹)" to "லிட்டருக்கான அடிப்படை விலை (₹)",
        "Add Milk Category" to "பால் வகையைச் சேர்",
        "Daily Procurement Stock Log" to "தினசரி கொள்முதல் இருப்பு விவரம்",
        "Cow Milk Volume (L)" to "பசுவின் பால் அளவு (லி)",
        "Buffalo Milk Volume (L)" to "எருமையின் பால் அளவு (லி)",
        "A2 Milk Volume (L)" to "A2 பால் அளவு (லி)",
        "Record Dynamic Volume Log" to "இருப்பை பதிவுசெய்",
        "Price Alerts & History" to "விலை மாற்ற வரலாறு",
        "Current Base Price" to "தற்போதைய அடிப்படை விலை",
        "Base Rate" to "அடைப்படை விலை",
        "Last Changed" to "கடைசியாக மாற்றப்பட்டது",
        "Cow" to "பசுவின் பால்",
        "Buffalo" to "எருமையின் பால்",
        "A2" to "A2 பால்",
        
        // General text
        "No Sales Found" to "விற்பனை எதுவும் இல்லை",
        "Add dairy sync profile" to "புதிய வாடிக்கையாளர் சுயவிவரம்",
        "Close" to "மூடு",
        "Cancel" to "ரத்து செய்",
        "Save" to "சேமி",
        "Registered buyers list: %s" to "பதிவுசெய்யப்பட்ட வாங்குபவர்கள்: %s",
        
        // System Branding & User Switching
        "System Branding" to "கணினி பிராண்டிங்",
        "Depot logo, custom bank/cooperative name & layout header titles" to "டிப்போ லோகோ, தனிப்பயன் வங்கி/கூட்டுறவு பெயர் & லேஅவுட் தலைப்புகள்",
        "System Header / App Name" to "கணினி தலைப்பு / பயன்பாட்டின் பெயர்",
        "Depot / Farm Address" to "டிப்போ / பண்ணை முகவரி",
        "Depot Logo" to "டிப்போ லோகோ",
        "Upload Logo Image" to "லோகோ படத்தை பதிவேற்றவும்",
        "Or Paste Logo URL" to "அல்லது லோகோ URL-ஐ ஒட்டவும்",
        "View as" to "பார்க்கும் பயனர்",
        "All" to "அனைத்தும்",
        "Loading branding config..." to "பிராண்டிங் அமைப்புகள் ஏற்றப்படுகின்றன...",
        "Branding settings" to "பிராண்டிங் அமைப்புகள்",
        "Branding settings saved successfully!" to "பிராண்டிங் அமைப்புகள் வெற்றிகரமாகச் சேமிக்கப்பட்டன!",
        "Save branding settings?" to "பிராண்டிங் அமைப்புகளைச் சேமிக்கவா?",
        "Logo image must be smaller than 2MB" to "லோகோ படம் 2MB-க்கு குறைவாக இருக்க வேண்டும்",
        "No Logo" to "லோகோ இல்லை"
    )

    fun translate(key: String, lang: String): String {
        if (lang == "ta") {
            val trimmedKey = key.trim()
            val translated = tamilMap[trimmedKey]
            if (translated != null) return translated
            
            // Check dynamic starts/ends
            for ((k, v) in tamilMap) {
                if (k.contains("%s")) {
                    val prefix = k.substringBefore("%s")
                    val suffix = k.substringAfter("%s")
                    if (prefix.isNotEmpty() && trimmedKey.startsWith(prefix)) {
                        return v
                    }
                }
            }
        }
        return key
    }
}

fun String.t(lang: String): String {
    return TranslationHelper.translate(this, lang)
}

fun String.t(lang: String, vararg args: Any): String {
    val translated = TranslationHelper.translate(this, lang)
    return try {
        String.format(translated, *args)
    } catch (e: Exception) {
        translated
    }
}
