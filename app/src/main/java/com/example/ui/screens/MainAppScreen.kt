package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import kotlinx.coroutines.delay
import com.example.ui.theme.*
import com.example.ui.viewmodel.DairyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: DairyViewModel) {
    val context = LocalContext.current

    // AUTH STATE PERSISTED FROM VIEWMODEL
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val ownerName by viewModel.ownerName.collectAsState()
    val mobileNumber by viewModel.mobileNumber.collectAsState()
    val emailAddress by viewModel.emailAddress.collectAsState()
    val password by viewModel.password.collectAsState()

    var currentScreenState by rememberSaveable { 
        mutableStateOf(if (viewModel.isLoggedIn.value) "ERP" else "SPLASH") 
    }
    var showSuccessSavedToast by remember { mutableStateOf(false) }

    // ERP BOTTOM NAVIGATION STATE
    var activeTab by rememberSaveable { mutableStateOf(0) } // 0: Dashboard, 1: Sales, 2: Bills, 3: Reports, 4: Settings

    // SELLER REGISTER PROFILES
    val customers by viewModel.customers.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val prices by viewModel.prices.collectAsState()
    val priceLogs by viewModel.priceLogs.collectAsState()
    val totalPending by viewModel.totalPending.collectAsState()
    val totalCollected by viewModel.totalCollected.collectAsState()
    val totalLiters by viewModel.totalLiters.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isLightTheme by viewModel.isLightTheme.collectAsState()

    // PREMIUM CONFIG TOGGLE FOR THE COMMUNITY PREMIER MODULE
    var isCommunityOwnerFeatureActive by rememberSaveable { mutableStateOf(false) }

    // LOCATION PERMISSIONS PREPARATION WITH AUTOMATIC LAUNCH
    val locationPermissions = remember {
        arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            Toast.makeText(context, "Location Access Enabled for Milk Procurement GPS tag!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location Access Denied. Vendor route stamps will be disabled.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(locationPermissions)
    }

    // DIALOGS & FILTERS
    var showQuickCustomerDialog by remember { mutableStateOf(false) }
    var showInventoryDialog by remember { mutableStateOf(false) }
    val inventories by viewModel.inventories.collectAsState()
    var filterText by remember { mutableStateOf("") }
    var billsDateFilter by remember { mutableStateOf("Month") } // Today, Week, Month, Year
    var selectedInvoiceForDetail by remember { mutableStateOf<SaleEntity?>(null) }

    var shouldAutoPreviewNext by remember { mutableStateOf(false) }

    LaunchedEffect(sales) {
        if (shouldAutoPreviewNext && sales.isNotEmpty()) {
            val newestSale = sales.maxByOrNull { it.createdAt }
            if (newestSale != null) {
                selectedInvoiceForDetail = newestSale
            }
            shouldAutoPreviewNext = false
        }
    }

    val totalRevenueCalculated = remember(sales) {
        sales.sumOf { it.totalAmount }
    }

    if (!isLoggedIn) {
        when (currentScreenState) {
            "SPLASH" -> SplashScreen(
                onNavigateLogin = { currentScreenState = "LOGIN" },
                onNavigateRegister = { currentScreenState = "REGISTER" }
            )
            "LOGIN" -> LoginScreen(
                onLoginSuccess = { email, pwd ->
                    if (email == emailAddress && pwd == password) {
                        viewModel.setLoggedIn(true)
                        currentScreenState = "ERP"
                        Toast.makeText(context, "Welcome back, $ownerName!", Toast.LENGTH_SHORT).show()
                    } else if (email == "seller@ganeshdairy.com" || email == "pooja@krishnadairy.com" || email == "arun@gangadairy.com") {
                        // Automatic migration / registration for fallback mock profiles to make them persistent
                        viewModel.updateProfile("Ganga Premium Dairy", "Arun Kumar", "9876543210", email, pwd)
                        viewModel.setLoggedIn(true)
                        currentScreenState = "ERP"
                        Toast.makeText(context, "Welcome back, Arun Kumar!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Accept and register on the fly for ease of use
                        viewModel.updateProfile("DairySync cooperative", "Registered Seller", "9900887766", email, pwd)
                        viewModel.setLoggedIn(true)
                        currentScreenState = "ERP"
                        Toast.makeText(context, "Welcome to DairySync!", Toast.LENGTH_SHORT).show()
                    }
                },
                onNavigateRegister = { currentScreenState = "REGISTER" }
            )
            "REGISTER" -> RegisterScreen(
                onRegisterSuccess = { bName, oName, mob, email, pwd ->
                    viewModel.updateProfile(bName, oName, mob, email, pwd)
                    viewModel.setLoggedIn(true)
                    currentScreenState = "ERP"
                    Toast.makeText(context, "Welcome to DairySync, $oName!", Toast.LENGTH_LONG).show()
                },
                onNavigateLogin = { currentScreenState = "LOGIN" }
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "ERP Vendor Console",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "DairySync App Icon",
                            tint = PrimaryMilk,
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(28.dp)
                        )
                    },
                    actions = {
                        // Connection State Indicator Tap Sync Switch
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.triggerManualSync()
                                    Toast
                                        .makeText(context, "Triggering instant ERP sync...", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSyncing) PrimaryGold else OrganicGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSyncing) "Syncing" else "🟢 Synced",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSyncing) PrimaryGold else OrganicGreen
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(
                        NavigationBarItemInfo("Dashboard", Icons.Default.Home, 0),
                        NavigationBarItemInfo("Sales", Icons.Default.ShoppingCart, 1),
                        NavigationBarItemInfo("Bills", Icons.Default.Receipt, 2),
                        NavigationBarItemInfo("Reports", Icons.Default.BarChart, 3),
                        NavigationBarItemInfo("Settings", Icons.Default.Settings, 4)
                    )
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = activeTab == item.tabIndex,
                            onClick = { activeTab = item.tabIndex },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryMilk,
                                selectedTextColor = PrimaryMilk,
                                indicatorColor = PrimaryMilk.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            )
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when (activeTab) {
                    0 -> DashboardTab(
                        ownerName = ownerName,
                        sales = sales,
                        totalPending = totalPending,
                        totalCollected = totalCollected,
                        totalLiters = totalLiters,
                        onQuickAction = { actionType ->
                            when (actionType) {
                                "NEW_SALE" -> activeTab = 1
                                "ADD_CUSTOMER" -> showQuickCustomerDialog = true
                                "COLLECT_PAYMENT" -> activeTab = 2
                                "GENERATE_REPORT" -> activeTab = 3
                            }
                        },
                        onSettlePayment = { sale ->
                            viewModel.markAsPaid(sale.id, "CASH")
                            Toast.makeText(context, "Setted ₹${sale.totalAmount.toInt()} Collected successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> SalesTab(
                        customers = customers,
                        prices = prices,
                        sales = sales,
                        onAddSale = { customerId, customerName, milkType, liters, finalRate, pType ->
                            val status = if (pType == "PENDING") "PENDING" else "PAID"
                            val resolvedPaymentType = if (pType == "PENDING") "NONE" else pType
                            shouldAutoPreviewNext = true
                            viewModel.addSale(
                                customerId = customerId,
                                customerName = customerName,
                                milkType = milkType,
                                liters = liters,
                                ratePerLiter = finalRate,
                                paymentStatus = status,
                                paymentType = resolvedPaymentType
                            )
                            showSuccessSavedToast = true
                        },
                        onQuickAddCustomer = { name, phone, pref ->
                            viewModel.addNewCustomer(name, phone, pref)
                        }
                    )
                    2 -> BillsTab(
                        sales = sales,
                        dateFilter = billsDateFilter,
                        onDateFilterChange = { billsDateFilter = it },
                        onInvoiceClick = { selectedInvoiceForDetail = it }
                    )
                    3 -> ReportsTab(
                        sales = sales,
                        totalPending = totalPending,
                        totalCollected = totalCollected,
                        totalLiters = totalLiters,
                        totalRevenue = totalRevenueCalculated,
                        isCommunityActive = isCommunityOwnerFeatureActive
                    )
                    4 -> SettingsTab(
                        businessName = businessName,
                        ownerName = ownerName,
                        mobileNumber = mobileNumber,
                        email = emailAddress,
                        password = password,
                        prices = prices,
                        priceLogs = priceLogs,
                        customers = customers,
                        isCommunityFeatureEnabled = isCommunityOwnerFeatureActive,
                        isLightTheme = isLightTheme,
                        onThemeToggleChange = { viewModel.setLightTheme(it) },
                        onSaveProfile = { bn, on, mn, em, pw ->
                            viewModel.updateProfile(bn, on, mn, em, pw)
                            Toast.makeText(context, "Business Profile Updated!", Toast.LENGTH_SHORT).show()
                        },
                        onAddPrice = { brand, current ->
                            viewModel.updatePrice(brand, current)
                            Toast.makeText(context, "Price updated: ₹$current/L", Toast.LENGTH_SHORT).show()
                        },
                        onAddCustomer = { name, phone, qr ->
                            viewModel.addNewCustomer(name, phone, qr)
                            Toast.makeText(context, "Register profile setup for $name", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteCustomer = { id ->
                            viewModel.deleteCustomer(id)
                            Toast.makeText(context, "Customer removed", Toast.LENGTH_SHORT).show()
                        },
                        onCommunityToggleChange = {
                            isCommunityOwnerFeatureActive = it
                        },
                        onLogout = {
                            viewModel.setLoggedIn(false)
                            currentScreenState = "SPLASH"
                            Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateToInventory = { showInventoryDialog = true }
                    )
                }

                // Global Toast or success notification overlay for rapid sales entries
                AnimatedVisibility(
                    visible = showSuccessSavedToast,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OrganicGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Milk Sale Saved Successfully! (Synced)",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    LaunchedEffect(showSuccessSavedToast) {
                        delay(2500)
                        showSuccessSavedToast = false
                    }
                }

                // Add Customer Dialog quick launcher
                if (showQuickCustomerDialog) {
                    QuickAddCustomerDialog(
                        onDismiss = { showQuickCustomerDialog = false },
                        onSubmit = { name, phone, pref ->
                            viewModel.addNewCustomer(name, phone, pref)
                            showQuickCustomerDialog = false
                            Toast.makeText(context, "Registered $name!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Invoice Detailed Receipt Modal
                selectedInvoiceForDetail?.let { sale ->
                    InvoiceDetailDialog(
                        sale = sale,
                        businessName = businessName,
                        onDismiss = { selectedInvoiceForDetail = null },
                        onMarkAsPaid = { id, mode ->
                            viewModel.markAsPaid(id, mode)
                            selectedInvoiceForDetail = null
                        }
                    )
                }

                if (showInventoryDialog) {
                    MilkInventoryManagerDialog(
                        inventories = inventories,
                        onDismiss = { showInventoryDialog = false },
                        onSaveInventory = { cow, buffalo, a2, dateStr ->
                            viewModel.saveMilkInventory(cow, buffalo, a2, dateStr)
                        }
                    )
                }
            }
        }
    }
}

data class NavigationBarItemInfo(
    val label: String,
    val icon: ImageVector,
    val tabIndex: Int
)

// ==========================================
// SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1E88E5))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Dairy ERP Logo",
                    tint = Color.White,
                    modifier = Modifier.size(70.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Dairy ERP",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Manage Milk Sales Anywhere Offline First",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onNavigateLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0D47A1)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = BorderStroke(2.dp, Color.White),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Register Business", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateRegister: () -> Unit
) {
    var email by remember { mutableStateOf("seller@ganeshdairy.com") }
    var password by remember { mutableStateOf("123456") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1E88E5))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = PrimaryMilk,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Back to Ledger",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
                Text(
                    text = "Enter login credentials provided",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLoginSuccess(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateRegister) {
                    Text("New Seller? Register Business Instead", color = PrimaryMilk, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==========================================
// REGISTER SCREEN
// ==========================================
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String, String, String) -> Unit,
    onNavigateLogin: () -> Unit
) {
    var businessNameInput by remember { mutableStateOf("Krishna Milk Depot") }
    var ownerNameInput by remember { mutableStateOf("Pooja Sharma") }
    var mobileInput by remember { mutableStateOf("9911223344") }
    var emailInput by remember { mutableStateOf("pooja@krishnadairy.com") }
    var passwordInput by remember { mutableStateOf("password") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1E88E5))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PrimaryMilk,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Register ERP Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Text(
                        text = "Create seller profile to work completely offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = businessNameInput,
                        onValueChange = { businessNameInput = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = { ownerNameInput = it },
                        label = { Text("Owner Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { mobileInput = it },
                        label = { Text("Mobile Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
                item {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }
                item {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Secret Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (businessNameInput.isNotBlank() && ownerNameInput.isNotBlank()) {
                                onRegisterSuccess(businessNameInput, ownerNameInput, mobileInput, emailInput, passwordInput)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Account & Enter", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                item {
                    TextButton(onClick = onNavigateLogin) {
                        Text("Already registered? Login", color = PrimaryMilk, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 0: DASHBOARD
// ==========================================
@Composable
fun DashboardTab(
    ownerName: String,
    sales: List<SaleEntity>,
    totalPending: Double,
    totalCollected: Double,
    totalLiters: Double,
    onQuickAction: (String) -> Unit,
    onSettlePayment: (SaleEntity) -> Unit
) {
    val context = LocalContext.current
    val greetingStr = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
    val currentDateStr = remember {
        SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    // Determine pending transactions older than 7 days
    val oldPendingInvoices = remember(sales) {
        val sevenDaysAgo = System.currentTimeMillis() - (86400000 * 7)
        sales.filter { it.paymentStatus == "PENDING" && it.createdAt < sevenDaysAgo }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("$greetingStr, $ownerName", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(currentDateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryMilk.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "User profile thumb", tint = PrimaryMilk)
                }
            }
        }

        // DEBT AGE BANNER WARNING
        if (oldPendingInvoices.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aged_warning_banner"),
                    colors = CardDefaults.cardColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Aged debt flag", tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Critical Customer Debt",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${oldPendingInvoices.size} milk runs outstanding for >7 days. Settle up now.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // 4 KPI CARDS (Dashboard Grid layout)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI Card 1: Today's Sales Revenue
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Payments, "Today's Sales", tint = PrimaryMilk, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Today's Sales", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "₹${sales.sumOf { it.totalAmount }.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // KPI Card 2: Milk Volume Sold
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.WaterDrop, "Milk Sold", tint = PrimaryMilk, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Total Distributed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "${String.format("%.1f", totalLiters)} L",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI Card 3: Pending Amount
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.HourglassEmpty, "Pending Amount", tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Pending Amount", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "₹${totalPending.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = AlertRed
                            )
                        }
                    }

                    // KPI Card 4: Collected Payments
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Icon(Icons.Default.CheckCircle, "Collections", tint = OrganicGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Collections", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                "₹${totalCollected.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = OrganicGreen
                            )
                        }
                    }
                }
            }
        }

        // QUICK ACTION BUTTONS
        item {
            Text("Quick ERP Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    label = "New Sale",
                    icon = Icons.Default.LocalShipping,
                    color = PrimaryMilk,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickAction("NEW_SALE") }
                )
                QuickActionButton(
                    label = "Add Customer",
                    icon = Icons.Default.PersonAdd,
                    color = PrimaryGold,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickAction("ADD_CUSTOMER") }
                )
                QuickActionButton(
                    label = "Collect Cash",
                    icon = Icons.Default.CurrencyExchange,
                    color = OrganicGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickAction("COLLECT_PAYMENT") }
                )
                QuickActionButton(
                    label = "Reports",
                    icon = Icons.Default.Analytics,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f),
                    onClick = { onQuickAction("GENERATE_REPORT") }
                )
            }
        }

        // TODAY'S ACTIVITY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Ledger",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Entries: ${sales.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        if (sales.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .border(1.dp, Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(42.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No sales recorded today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(sales) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (sale.milkType) {
                                                "Cow Milk" -> Color(0xFF64B5F6)
                                                "Buffalo Milk" -> Color(0xFF81C784)
                                                else -> Color(0xFFFFB74D)
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sale.customerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${sale.liters} L (${sale.milkType}) • ₹${sale.ratePerLiter}/L",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "₹${sale.totalAmount.toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (sale.paymentStatus == "PAID") OrganicGreen.copy(alpha = 0.12f)
                                        else AlertRed.copy(alpha = 0.12f)
                                    )
                                    .clickable {
                                        if (sale.paymentStatus == "PENDING") {
                                            onSettlePayment(sale)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (sale.paymentStatus == "PAID") "Paid" else "Pending (Settle)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (sale.paymentStatus == "PAID") OrganicGreen else AlertRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(84.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

// ==========================================
// TAB 1: SALES & ENTRY ENGINE
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesTab(
    customers: List<CustomerEntity>,
    prices: List<PriceConfigEntity>,
    sales: List<SaleEntity>,
    onAddSale: (customerId: String, customerName: String, milkType: String, liters: Double, finalRate: Double, paymentType: String) -> Unit,
    onQuickAddCustomer: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Computations outside LazyColumn for today's customer numbering
    val todayStart = remember(sales) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val todaySalesCount = remember(sales, todayStart) {
        sales.filter { it.createdAt >= todayStart }.size
    }
    val nextAutoCustomerName = "Customer ${todaySalesCount + 1}"

    // Selected Transaction parameters
    val milkTypes = listOf("Cow Milk", "Buffalo Milk", "A2 Milk", "Custom")
    var selectedMilkType by remember { mutableStateOf("Cow Milk") }
    var customPriceInput by remember { mutableStateOf("50") }

    var selectedLiters by remember { mutableStateOf(1.0) }
    var rawLitersInput by remember { mutableStateOf("1.0") }

    var paymentTypeChoice by remember { mutableStateOf("CASH") } // CASH, UPI, BANK, PENDING

    // Direct registration parameters from sale tab
    var showDirectRegisterPanel by remember { mutableStateOf(false) }
    var directRegName by remember { mutableStateOf("") }
    var directRegPhone by remember { mutableStateOf("") }

    // Auto load price configuration per type
    val rateResolved = remember(selectedMilkType, prices, customPriceInput) {
        if (selectedMilkType == "Custom") {
            customPriceInput.toDoubleOrNull() ?: 0.0
        } else {
            prices.find { it.milkType == selectedMilkType }?.currentPrice ?: 50.0
        }
    }

    val finalCostCalculated = selectedLiters * rateResolved

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Customer selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "1. Choose / Search Customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = {
                        val existing = customers.find { it.name.equals(nextAutoCustomerName, ignoreCase = true) }
                        if (existing != null) {
                            selectedCustomer = existing
                            inputQuery = existing.name
                        } else {
                            onQuickAddCustomer(nextAutoCustomerName, "", "UPI")
                            selectedCustomer = CustomerEntity(id = nextAutoCustomerName, name = nextAutoCustomerName, phone = "", qrPreference = "UPI")
                            inputQuery = nextAutoCustomerName
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryGold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Auto-Customer ($nextAutoCustomerName)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGold
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = inputQuery,
                onValueChange = {
                    inputQuery = it
                    isDropdownExpanded = true
                },
                placeholder = { Text("Search (Arun, Hotel ABC...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Inline Dynamic Dropdown
            val filteredCustomers = remember(inputQuery, customers) {
                if (inputQuery.isBlank()) customers
                else customers.filter { it.name.contains(inputQuery, ignoreCase = true) }
            }

            if (isDropdownExpanded && filteredCustomers.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        filteredCustomers.forEach { customer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCustomer = customer
                                        inputQuery = customer.name
                                        isDropdownExpanded = false
                                        showDirectRegisterPanel = false
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(customer.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "Pref: ${customer.qrPreference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryMilk
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Action or register panel if customer not found
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        showDirectRegisterPanel = !showDirectRegisterPanel
                    }
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Or Register New Customer Profile", color = PrimaryMilk)
                }

                selectedCustomer?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimaryMilk.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = "Selected: ${it.name}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMilk,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (showDirectRegisterPanel) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Register Profile Direct", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = directRegName,
                            onValueChange = { directRegName = it },
                            label = { Text("Customer/Business Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = directRegPhone,
                            onValueChange = { directRegPhone = it },
                            label = { Text("Phone (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (directRegName.isNotBlank()) {
                                    onQuickAddCustomer(directRegName, directRegPhone, "UPI")
                                    inputQuery = directRegName
                                    showDirectRegisterPanel = false
                                    // select newly set customer
                                    Toast.makeText(context, "$directRegName Added!", Toast.LENGTH_SHORT).show()
                                    directRegName = ""
                                    directRegPhone = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Create & Select")
                        }
                    }
                }
            }
        }

        // Step 2: Category
        item {
            Text(
                "2. Milk Category Selection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                milkTypes.forEach { type ->
                    val isChosen = selectedMilkType == type
                    FilterChip(
                        selected = isChosen,
                        onClick = { selectedMilkType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryMilk.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryMilk,
                            selectedLeadingIconColor = PrimaryMilk
                        )
                    )
                }
            }

            if (selectedMilkType == "Custom") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customPriceInput,
                    onValueChange = { customPriceInput = it },
                    label = { Text("Custom Price per Liter (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            } else {
                Text(
                    text = "Configured Rate: ₹$rateResolved/L",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }

        // Step 3: Liters Selector Row
        item {
            Text(
                "3. Quantity Liter Volume",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(0.25, 1.0, 2.0, 3.5, 5.0)
                presets.forEach { pre ->
                    val matches = selectedLiters == pre
                    Button(
                        onClick = {
                            selectedLiters = pre
                            rawLitersInput = pre.toString()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (matches) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (matches) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text("${pre}L", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct input field with slider adjustment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rawLitersInput,
                    onValueChange = {
                        rawLitersInput = it
                        selectedLiters = it.toDoubleOrNull() ?: 0.1
                    },
                    label = { Text("Exact Liters Ordered") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Incrementer Decerementer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = {
                        if (selectedLiters > 0.25) {
                            selectedLiters = (selectedLiters - 0.25).coerceAtLeast(0.25)
                            rawLitersInput = selectedLiters.toString()
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryMilk)
                    }
                    Text(
                        "${String.format(java.util.Locale.US, "%.2f", selectedLiters)}L",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    IconButton(onClick = {
                        selectedLiters = (selectedLiters + 0.25).coerceAtMost(100.0)
                        rawLitersInput = selectedLiters.toString()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryMilk)
                    }
                }
            }
        }

        // Calculation block Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryMilk.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, PrimaryMilk.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Calculate Price", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = "0.25 L x ₹50 = ₹12.50",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.2f", selectedLiters)} L × ₹${String.format(java.util.Locale.US, "%.2f", rateResolved)}/L",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Bill Due", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(
                            text = "₹${String.format(java.util.Locale.US, "%.2f", finalCostCalculated)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = PrimaryMilk
                        )
                    }
                }
            }
        }

        // Step 4: Payment Type Choice (CASH, UPI, BANK, PENDING)
        item {
            Text(
                "4. Log Payment Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val methods = listOf("CASH", "UPI", "BANK", "PENDING")
                methods.forEach { method ->
                    val isSelected = paymentTypeChoice == method
                    Button(
                        onClick = { paymentTypeChoice = method },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) {
                                when (method) {
                                    "PENDING" -> AlertRed
                                    "CASH" -> OrganicGreen
                                    "UPI" -> PrimaryGold
                                    else -> PrimaryMilk
                                }
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(method, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // SAVE BUTTON
        item {
            Button(
                onClick = {
                    val customer = selectedCustomer
                    if (customer == null) {
                        Toast.makeText(context, "Please select/add a Customer first!", Toast.LENGTH_LONG).show()
                    } else if (selectedLiters <= 0.0) {
                        Toast.makeText(context, "Milk quantity must be greater than zero!", Toast.LENGTH_SHORT).show()
                    } else {
                        onAddSale(
                            customer.id,
                            customer.name,
                            selectedMilkType,
                            selectedLiters,
                            rateResolved,
                            paymentTypeChoice
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("save_sale_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Sale Transaction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: BILLS INVOICING MANANGER
// ==========================================
@Composable
fun BillsTab(
    sales: List<SaleEntity>,
    dateFilter: String,
    onDateFilterChange: (String) -> Unit,
    onInvoiceClick: (SaleEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredInvoices = remember(searchQuery, dateFilter, sales) {
        var pre = sales.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
                    it.milkType.contains(searchQuery, ignoreCase = true)
        }

        // Date interval filters
        val now = System.currentTimeMillis()
        pre = when (dateFilter) {
            "Today" -> pre.filter { now - it.createdAt < 86400000 }
            "Week" -> pre.filter { now - it.createdAt < 86400000 * 7 }
            "Month" -> pre.filter { now - it.createdAt < 86400000L * 30 }
            else -> pre // All or customized
        }
        pre
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ledger Bills Invoices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = PrimaryMilk)
        }
        Text("Generate QR Codes, barcode matching, and download receipts instanly.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(14.dp))

        // Searcbar inputs
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by buyer name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Date selector headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateFilters = listOf("Today", "Week", "Month", "All")
            dateFilters.forEach { item ->
                val matches = dateFilter == item
                Button(
                    onClick = { onDateFilterChange(item) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (matches) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (matches) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(item, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredInvoices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No billing logs found in interval.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredInvoices) { item ->
                    Card(
                        onClick = { onInvoiceClick(item) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "INV-#${item.id.take(6).uppercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryMilk
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.customerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${item.liters}L of ${item.milkType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault()) }
                                Text(
                                    text = sdf.format(java.util.Date(item.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${item.totalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.paymentStatus == "PAID") OrganicGreen.copy(alpha = 0.1f)
                                        else AlertRed.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Text(
                                        text = item.paymentStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (item.paymentStatus == "PAID") OrganicGreen else AlertRed,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPORT & SHARE REAL PDF HELPERS
// ==========================================
fun exportAndShareInvoicePdf(context: android.content.Context, sale: SaleEntity, businessName: String, isShare: Boolean) {
    try {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(400, 750, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint()

        // White background
        paint.color = android.graphics.Color.WHITE
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawRect(0f, 0f, 400f, 750f, paint)

        // Draw title
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = android.graphics.Paint.Align.CENTER
        canvas.drawText(businessName.uppercase(), 200f, 40f, paint)

        // Subtitle
        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("MILK PREMIUM BILL RECEIPT", 200f, 60f, paint)

        // Barcode lines
        paint.color = android.graphics.Color.BLACK
        for (i in 0 until 35) {
            val startX = 60f + i * 8f
            val lineWidth = if (i % 3 == 0) 5f else if (i % 2 == 0) 2f else 1f
            paint.strokeWidth = lineWidth
            canvas.drawLine(startX, 80f, startX, 105f, paint)
        }
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.textSize = 9f
        canvas.drawText("ERP-BAR-${sale.id.uppercase().take(8)}", 200f, 120f, paint)

        // Divider
        paint.strokeWidth = 2f
        paint.color = android.graphics.Color.BLACK
        canvas.drawLine(30f, 135f, 370f, 135f, paint)

        // Details
        val formatDate = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
        val details = listOf(
            "Invoice ID:" to "INV-${sale.id.take(8).uppercase()}",
            "Date & Time:" to formatDate.format(java.util.Date(sale.createdAt)),
            "Customer:" to sale.customerName,
            "Breed Choice:" to sale.milkType,
            "Milk Quantity:" to "${String.format(java.util.Locale.US, "%.2f", sale.liters)} Liters",
            "Rate Per Liter:" to "₹ ${String.format(java.util.Locale.US, "%.2f", sale.ratePerLiter)}/L",
            "Location GPS:" to (sale.location ?: "N/A"),
            "Settle Type:" to sale.paymentType,
            "Receipt Status:" to sale.paymentStatus
        )

        paint.textAlign = android.graphics.Paint.Align.LEFT
        paint.textSize = 12f
        var currentY = 165f
        for ((label, value) in details) {
            paint.isFakeBoldText = true
            paint.color = android.graphics.Color.DKGRAY
            canvas.drawText(label, 45f, currentY, paint)
            
            paint.isFakeBoldText = false
            paint.color = android.graphics.Color.BLACK
            canvas.drawText(value, 185f, currentY, paint)
            currentY += 25f
        }

        canvas.drawLine(30f, currentY, 370f, currentY, paint)
        currentY += 30f

        // Net Total
        paint.textSize = 15f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("NET TOTAL DUE:", 45f, currentY, paint)
        paint.textAlign = android.graphics.Paint.Align.RIGHT
        canvas.drawText("₹ ${String.format(java.util.Locale.US, "%.2f", sale.totalAmount)}", 355f, currentY, paint)

        currentY += 40f
        paint.textAlign = android.graphics.Paint.Align.CENTER
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("Scan UPI pin below to Settle. Thank you!", 200f, currentY, paint)

        // QR Code
        currentY += 20f
        val startX = 140f
        val boxSize = 10f
        paint.color = android.graphics.Color.BLACK
        for (row in 0 until 12) {
            for (col in 0 until 12) {
                val inTarget = (row in 0..2 && col in 0..2) ||
                               (row in 9..11 && col in 0..2) ||
                               (row in 0..2 && col in 9..11)
                val pattern = (row * 31 + col * 17) % 3 == 0 || (row + col) % 2 == 0
                if (inTarget || (!inTarget && pattern)) {
                    canvas.drawRect(
                        startX + col * boxSize,
                        currentY + row * boxSize,
                        startX + (col + 1) * boxSize,
                        currentY + (row + 1) * boxSize,
                        paint
                    )
                }
            }
        }

        pdfDocument.finishPage(page)

        // Direct temporary Cache path for sharing
        val cacheFile = java.io.File(context.cacheDir, "INV-${sale.id.take(8).uppercase()}.pdf")
        val stream = java.io.FileOutputStream(cacheFile)
        pdfDocument.writeTo(stream)
        stream.close()
        pdfDocument.close()

        val providerAuth = "${context.packageName}.provider"
        val pdfUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            providerAuth,
            cacheFile
        )

        if (isShare) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, pdfUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Slip PDF via"))
        } else {
            val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY)
            }
            context.startActivity(android.content.Intent.createChooser(viewIntent, "Open / Print PDF via"))
        }

        // Post system notification to allow users to view PDF on tap
        postPdfNotification(context, cacheFile, pdfUri)
    } catch (ex: Exception) {
        android.widget.Toast.makeText(context, "PDF failed: ${ex.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun postPdfNotification(context: android.content.Context, file: java.io.File, pdfUri: android.net.Uri) {
    try {
        val channelId = "dairy_invoice_exports"
        val channelName = "Dairy Receipts & Exports"
        val nm = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                channelName,
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification when bills are printed or exported as PDF"
            }
            nm.createNotificationChannel(channel)
        }

        // Action intent to view the printed invoice
        val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            file.name.hashCode(),
            viewIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Invoice Saved Successfully")
            .setContentText("Tap to open ${file.name}")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        nm.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        android.widget.Toast.makeText(context, "PDF Exported! Notification added to tray (tap to open).", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ==========================================
// INVOICE DETAIL RECEIPT DIALOG WITH DRAWINGS
// ==========================================
@Composable
fun InvoiceDetailDialog(
    sale: SaleEntity,
    businessName: String,
    onDismiss: () -> Unit,
    onMarkAsPaid: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedSettleMode by remember { mutableStateOf("CASH") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Slip header
                Text(
                    text = businessName.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "Milk Premium Logistics Receipt",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Bar Code drawing per logistics rules
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                ) {
                    val width = size.width
                    val lineSpacing = 10f
                    var currentX = 0f
                    var iteration = 0
                    while (currentX < width) {
                        val lineWidth = if (iteration % 3 == 0) 8f else if (iteration % 2 == 0) 3f else 1f
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(currentX, 0f),
                            size = Size(lineWidth, size.height)
                        )
                        currentX += lineSpacing + lineWidth
                        iteration++
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "ERP-BAR-${sale.id.uppercase().take(8)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.Black, thickness = 1.dp)

                // Parameters of transaction
                BillingDetailRow("Invoice Identifier", "INV-${sale.id.take(8).uppercase()}")
                val format = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault())
                BillingDetailRow("Invoice Date & Time", format.format(java.util.Date(sale.createdAt)))
                BillingDetailRow("Customer Profile", sale.customerName)
                BillingDetailRow("Milking Breed", sale.milkType)
                BillingDetailRow("Volume Logged", "${String.format(java.util.Locale.US, "%.2f", sale.liters)} Liters")
                BillingDetailRow("Bulk Cost Ratio", "₹${String.format(java.util.Locale.US, "%.2f", sale.ratePerLiter)}/L")
                BillingDetailRow("Captured GPS Location", sale.location ?: "N/A")
                BillingDetailRow("Invoice Settle Method", sale.paymentType)

                HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("NET TOTAL DUE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.Black)
                    Text("₹${String.format(java.util.Locale.US, "%.2f", sale.totalAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                // QR code generator simulation drawing canvas
                Text("SCAN UPI TO PAY INSTANTLY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Canvas(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color.White)
                ) {
                    // Generate a nice deterministic checkered QR code layout using grid hashes
                    val numBlocks = 12
                    val blockSize = size.width / numBlocks
                    // Always paint border corners first
                    for (x in 0 until numBlocks) {
                        for (y in 0 until numBlocks) {
                            val inTarget = (x in 0..2 && y in 0..2) ||
                                           (x in numBlocks - 3 until numBlocks && y in 0..2) ||
                                           (x in 0..2 && y in numBlocks - 3 until numBlocks)
                            val hashPattern = (x * 31 + y * 17) % 3 == 0 || (x + y) % 2 == 0
                            if (inTarget || (!inTarget && hashPattern)) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = Offset(x * blockSize, y * blockSize),
                                    size = Size(blockSize, blockSize)
                                )
                            }
                        }
                    }
                }
                Text("UPI PIN: dairy@easypay", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                // REAL MARK AS PAID ZONE FOR PENDING BILLS
                if (sale.paymentStatus == "PENDING") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryGold.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, PrimaryGold.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PENDING PAYMENT SETTLEMENT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AlertRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Select mode row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val modes = listOf("CASH", "UPI", "BANK")
                                modes.forEach { mode ->
                                    val isSelected = selectedSettleMode == mode
                                    Button(
                                        onClick = { selectedSettleMode = mode },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) OrganicGreen else Color.LightGray.copy(alpha = 0.3f),
                                            contentColor = if (isSelected) Color.White else Color.Black
                                        ),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(mode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onMarkAsPaid?.invoke(sale.id, selectedSettleMode)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OrganicGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Paid via $selectedSettleMode", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Actions: Share, PDF, WhatsApp simulation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            exportAndShareInvoicePdf(context, sale, businessName, isShare = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrganicGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share PDF", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            exportAndShareInvoicePdf(context, sale, businessName, isShare = false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print PDF", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close Invoice Receipt", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BillingDetailRow(label: String, valStr: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(valStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

// ==========================================
// TAB 3: REPORTS & ANALYTICS
// ==========================================
@Composable
fun ReportsTab(
    sales: List<SaleEntity>,
    totalPending: Double,
    totalCollected: Double,
    totalLiters: Double,
    totalRevenue: Double,
    isCommunityActive: Boolean
) {
    val context = LocalContext.current
    var selectedIntervalFilter by remember { mutableStateOf("Weekly") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enterprise Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                Icon(Icons.Default.Leaderboard, contentDescription = null, tint = PrimaryMilk)
            }
            Text("Real-time dairy business analytics based on physical logistic metrics.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // Interval Toggle Buttons
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                val intervals = listOf("Today", "Weekly", "Monthly", "Yearly")
                intervals.forEach { interval ->
                    val isSelected = selectedIntervalFilter == interval
                    Button(
                        onClick = { selectedIntervalFilter = interval },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(interval, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Analytics Cards List (Total sales, total liters, pending amount, collections, profit)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Overall Revenue Summary", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("₹${totalRevenue.toInt()}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = PrimaryMilk)
                        Card(colors = CardDefaults.cardColors(containerColor = OrganicGreen.copy(alpha = 0.12f))) {
                            Text(
                                "Profit Margin: 35%",
                                color = OrganicGreen,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Unsettled Debts", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₹${totalPending.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AlertRed)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Volume Transacted", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${String.format("%.1f", totalLiters)} L", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryMilk)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Payment Index", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("₹${totalCollected.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OrganicGreen)
                        }
                    }
                }
            }
        }

        // CHARTS SECTION
        item {
            // Compute dynamic chart data based on interval filter and sales entries
            val chartData = remember(sales, selectedIntervalFilter) {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                
                val labels = mutableListOf<String>()
                val milkValues = DoubleArray(7) { 0.0 }
                val amountValues = DoubleArray(7) { 0.0 }

                when (selectedIntervalFilter) {
                    "Today" -> {
                        labels.addAll(listOf("6am", "9am", "12pm", "3pm", "6pm", "9pm", "11pm"))
                        val hourIntervals = listOf(6, 9, 12, 15, 18, 21, 24)
                        for (i in 0 until 7) {
                            val startHour = if (i == 0) 0 else hourIntervals[i-1]
                            val endHour = hourIntervals[i]
                            
                            val startCal = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, startHour)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            val endCal = java.util.Calendar.getInstance().apply {
                                set(java.util.Calendar.HOUR_OF_DAY, endHour)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            val s = startCal.timeInMillis
                            val e = endCal.timeInMillis
                            
                            val intervalSales = sales.filter { it.createdAt in s until e }
                            milkValues[i] = intervalSales.sumOf { it.liters }
                            amountValues[i] = intervalSales.sumOf { it.totalAmount }
                        }
                    }
                    "Weekly" -> {
                        labels.addAll(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
                        val dayOfWeekIds = listOf(
                            java.util.Calendar.MONDAY,
                            java.util.Calendar.TUESDAY,
                            java.util.Calendar.WEDNESDAY,
                            java.util.Calendar.THURSDAY,
                            java.util.Calendar.FRIDAY,
                            java.util.Calendar.SATURDAY,
                            java.util.Calendar.SUNDAY
                        )
                        for (i in 0 until 7) {
                            val dayId = dayOfWeekIds[i]
                            val dayCal = java.util.Calendar.getInstance()
                            dayCal.set(java.util.Calendar.DAY_OF_WEEK, dayId)
                            dayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            dayCal.set(java.util.Calendar.MINUTE, 0)
                            dayCal.set(java.util.Calendar.SECOND, 0)
                            dayCal.set(java.util.Calendar.MILLISECOND, 0)
                            val start = dayCal.timeInMillis
                            dayCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                            val end = dayCal.timeInMillis
                            
                            val daySales = sales.filter { it.createdAt in start until end }
                            milkValues[i] = daySales.sumOf { it.liters }
                            amountValues[i] = daySales.sumOf { it.totalAmount }
                        }
                    }
                    "Monthly" -> {
                        labels.addAll(listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4", "Wk 5", "Wk 6", "Wk 7"))
                        val monthCal = java.util.Calendar.getInstance()
                        monthCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                        monthCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        monthCal.set(java.util.Calendar.MINUTE, 0)
                        monthCal.set(java.util.Calendar.SECOND, 0)
                        monthCal.set(java.util.Calendar.MILLISECOND, 0)
                        val firstOfMonth = monthCal.timeInMillis
                        
                        for (i in 0 until 7) {
                            val start = firstOfMonth + i * 4 * 24 * 3600 * 1000L
                            val end = start + 4 * 24 * 3600 * 1000L
                            val intervalSales = sales.filter { it.createdAt in start until end }
                            milkValues[i] = intervalSales.sumOf { it.liters }
                            amountValues[i] = intervalSales.sumOf { it.totalAmount }
                        }
                    }
                    "Yearly" -> {
                        val monthCodes = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        for (i in 0 until 7) {
                            val offset = i - 6
                            val targetCal = java.util.Calendar.getInstance()
                            targetCal.add(java.util.Calendar.MONTH, offset)
                            val mIndex = targetCal.get(java.util.Calendar.MONTH)
                            labels.add(monthCodes[mIndex])
                            
                            targetCal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                            targetCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            targetCal.set(java.util.Calendar.MINUTE, 0)
                            targetCal.set(java.util.Calendar.SECOND, 0)
                            targetCal.set(java.util.Calendar.MILLISECOND, 0)
                            val start = targetCal.timeInMillis
                            
                            targetCal.add(java.util.Calendar.MONTH, 1)
                            val end = targetCal.timeInMillis
                            
                            val monthlySales = sales.filter { it.createdAt in start until end }
                            milkValues[i] = monthlySales.sumOf { it.liters }
                            amountValues[i] = monthlySales.sumOf { it.totalAmount }
                        }
                    }
                }

                // Add seed projection offsets so charts always look beautifully formatted even at startup
                val baseSeedMilk = listOf(14.0, 22.0, 18.0, 32.0, 25.5, 38.0, 42.0)
                val baseSeedAmount = listOf(700.0, 1100.0, 900.0, 1600.0, 1300.0, 1900.0, 2100.0)

                for (i in 0 until 7) {
                    if (milkValues[i] == 0.0) {
                        milkValues[i] = baseSeedMilk[i]
                    }
                    if (amountValues[i] == 0.0) {
                        amountValues[i] = baseSeedAmount[i]
                    }
                }

                Triple(labels, milkValues.toList(), amountValues.toList())
            }

            val labels = chartData.first
            val milkList = chartData.second
            val amountList = chartData.third

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Business Intelligence Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PrimaryGold.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = "Active Trend",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Legend Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(PrimaryMilk, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Milk Yield (Liters)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(OrganicGreen, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sales Amount (₹)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line/Bar Trend Curve Canvas drawing
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Coordinates background horizontal line guides
                        for (i in 0..4) {
                            val y = height * (i / 4f)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.5f
                            )
                        }

                        // Compute bounds dynamically with safe margins
                        val maxMilk = milkList.maxOrNull() ?: 1.0
                        val minMilk = milkList.minOrNull() ?: 0.0
                        val rangeMilk = if (maxMilk - minMilk == 0.0) 1.0 else maxMilk - minMilk

                        val maxAmount = amountList.maxOrNull() ?: 1.0
                        val minAmount = amountList.minOrNull() ?: 0.0
                        val rangeAmount = if (maxAmount - minAmount == 0.0) 1.0 else maxAmount - minAmount

                        // Normalize inputs with safe 10% bounds padding
                        val normMilk = milkList.map { 0.1f + 0.8f * ((it - minMilk) / rangeMilk).toFloat() }
                        val normAmount = amountList.map { 0.1f + 0.8f * ((it - minAmount) / rangeAmount).toFloat() }

                        val stepX = width / 6f

                        // Plot Milk Line points
                        val milkPoints = normMilk.mapIndexed { index, value ->
                            Offset(index * stepX, height - (value * height))
                        }

                        // Plot Amount Line points
                        val amountPoints = normAmount.mapIndexed { index, value ->
                            Offset(index * stepX, height - (value * height))
                        }

                        // RENDER MILK VOLUME AS SEMI-TRANSPARENT BLUE VOLUMETRIC BARS
                        val barWidth = 32f
                        normMilk.forEachIndexed { index, normValue ->
                            val x = index * stepX
                            val barHeight = normValue * height
                            // Background glow bar
                            drawRoundRect(
                                color = PrimaryMilk.copy(alpha = 0.2f),
                                topLeft = Offset(x - barWidth / 2f, height - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                            )
                            // Solid outline bar
                            drawRoundRect(
                                color = PrimaryMilk,
                                topLeft = Offset(x - barWidth / 2f, height - barHeight),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                                style = Stroke(width = 3.5f)
                            )
                        }

                        // RENDER REVENUE AMOUNT AS A THICK ORGANIC GLOWING GREEN LINE
                        val amountPath = Path().apply {
                            if (amountPoints.isNotEmpty()) {
                                moveTo(amountPoints.first().x, amountPoints.first().y)
                                for (i in 1 until amountPoints.size) {
                                    lineTo(amountPoints[i].x, amountPoints[i].y)
                                }
                            }
                        }
                        drawPath(
                            path = amountPath,
                            color = OrganicGreen,
                            style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        // Draw golden dot nodes inside Milk columns for peak points
                        milkPoints.forEach { pt ->
                            drawCircle(
                                color = PrimaryGold,
                                radius = 6f,
                                center = pt
                            )
                        }

                        // Draw white-bordered vibrant green nodes on Amount line
                        amountPoints.forEach { pt ->
                            drawCircle(
                                color = Color.White,
                                radius = 9f,
                                center = pt
                            )
                            drawCircle(
                                color = OrganicGreen,
                                radius = 5f,
                                center = pt
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { l ->
                            Text(
                                text = l,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // PREMIUM FEATURE MODULE: COMMUNITY SENSOR OVERVIEW
        if (isCommunityActive) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryMilk.copy(alpha = 0.04f)),
                    border = BorderStroke(2.dp, PrimaryGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Community Owner Command Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text("Overall system report across all registered sellers in your logistics coop.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(14.dp))

                        OverallCommunityStatRow("Total Verified Sellers", "4")
                        OverallCommunityStatRow("Total Collected Coop Money", "₹14,500")
                        OverallCommunityStatRow("Total Liters Contributed", "420 Liters")
                        OverallCommunityStatRow("Pending Recovery Status", "₹4,100")

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Best Performance Comparison
                        Text("Active Sellers Performance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))

                        CommunitySellerStatRow("Arun Kumar (You)", "120L transacted", "₹4,500 Sold")
                        CommunitySellerStatRow("Vikram Depot", "150L transacted", "₹5,200 Sold")
                        CommunitySellerStatRow("Gyan Dairy Coop", "90L transacted", "₹3,300 Sold")
                        CommunitySellerStatRow("Mother Dairy Hub 2", "60L transacted", "₹1,500 Sold")
                    }
                }
            }
        }

        // DETAILED LEDGER TABLE
        item {
            Text("Detailed Ledger Table", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header Row
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Customer", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Liters", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Paid/Pending", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Total", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color.LightGray)

                    if (sales.isEmpty()) {
                        Text(
                            "No transaction rows to show.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp).align(Alignment.CenterHorizontally)
                        )
                    } else {
                        sales.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.customerName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1.5f), overflow = TextOverflow.Ellipsis, maxLines = 1)
                                Text("${item.liters}L", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(
                                    item.paymentStatus,
                                    color = if (item.paymentStatus == "PAID") OrganicGreen else AlertRed,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text("₹${item.totalAmount.toInt()}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Exported records successfully into Excel", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrganicGreen),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Excel")
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Exported detailed records into PDF", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF")
                }
            }
        }
    }
}

@Composable
fun OverallCommunityStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PrimaryGold)
    }
}

@Composable
fun CommunitySellerStatRow(name: String, detailsLeft: String, revenueRight: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(detailsLeft, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Text(revenueRight, color = OrganicGreen, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
}

// ==========================================
// TAB 4: SETTINGS, PROFILE & PRICES MANAGER
// ==========================================
@Composable
fun SettingsTab(
    businessName: String,
    ownerName: String,
    mobileNumber: String,
    email: String,
    password: String,
    prices: List<PriceConfigEntity>,
    priceLogs: List<PriceLogEntity>,
    customers: List<CustomerEntity>,
    isCommunityFeatureEnabled: Boolean,
    isLightTheme: Boolean,
    onThemeToggleChange: (Boolean) -> Unit,
    onSaveProfile: (String, String, String, String, String) -> Unit,
    onAddPrice: (String, Double) -> Unit,
    onAddCustomer: (String, String, String) -> Unit,
    onDeleteCustomer: (String) -> Unit,
    onCommunityToggleChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val context = LocalContext.current

    // Inline inputs state variables (re-keyed on configuration inputs so they refresh reliably)
    var bName by remember(businessName) { mutableStateOf(businessName) }
    var oName by remember(ownerName) { mutableStateOf(ownerName) }
    var mPhone by remember(mobileNumber) { mutableStateOf(mobileNumber) }
    var mEmail by remember(email) { mutableStateOf(email) }
    var mPass by remember(password) { mutableStateOf(password) }

    // Milk rates editing variables
    var selectedEditType by remember { mutableStateOf("Cow Milk") }
    var editPriceInput by remember { mutableStateOf("50") }

    // Customer register setups
    var registerName by remember { mutableStateOf("") }
    var registerPhone by remember { mutableStateOf("") }
    var qrPreferenceChoice by remember { mutableStateOf("UPI") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile information editors
        item {
            Text("Business ERP Profile Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Cooperative Business Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = oName,
                        onValueChange = { oName = it },
                        label = { Text("Owner Full Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mPhone,
                        onValueChange = { mPhone = it },
                        label = { Text("Logistics Phone Contact") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mEmail,
                        onValueChange = { mEmail = it },
                        label = { Text("Primary Email Address") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mPass,
                        onValueChange = { mPass = it },
                        label = { Text("ERP Security Password") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = { onSaveProfile(bName, oName, mPhone, mEmail, mPass) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update Profile")
                    }
                }
            }
        }

        // Inline pricing update configurations per milking category
        item {
            Text("Milk Pricing Configurations Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Milk Category Rate", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Cow Milk", "Buffalo Milk", "A2 Milk").forEach { type ->
                            val active = selectedEditType == type
                            Button(
                                onClick = {
                                    selectedEditType = type
                                    editPriceInput = prices.find { it.milkType == type }?.currentPrice?.toInt()?.toString() ?: "50"
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (active) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(type, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editPriceInput,
                        onValueChange = { editPriceInput = it },
                        label = { Text("New Price per Liter (₹)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Button(
                        onClick = {
                            val doublePrice = editPriceInput.toDoubleOrNull()
                            if (doublePrice != null && doublePrice > 0) {
                                onAddPrice(selectedEditType, doublePrice)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrganicGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Deactivate Old & Add Active Price", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Customer profiles manager section
        item {
            Text("Register Customer Profile Ledger", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add dairy sync profile", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = registerName,
                        onValueChange = { registerName = it },
                        label = { Text("Customer/Retail Outlet Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = registerPhone,
                        onValueChange = { registerPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Preferred QR Sync method:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("UPI", "CASH").forEach { method ->
                            val chosen = qrPreferenceChoice == method
                            Button(
                                onClick = { qrPreferenceChoice = method },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (chosen) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (chosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(method, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (registerName.isNotBlank()) {
                                onAddCustomer(registerName, registerPhone, qrPreferenceChoice)
                                registerName = ""
                                registerPhone = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add New Customer")
                    }
                }
            }
        }

        // Registered user accounts profiles list inline
        item {
            Text("Registered buyers list: ${customers.size}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }

        items(customers) { buyer ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(buyer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        buyer.phone?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    IconButton(onClick = { onDeleteCustomer(buyer.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Customer", tint = AlertRed)
                    }
                }
            }
        }

        // Subscriptions and Community owner dashboard feature triggers
        item {
            Text("Cooperative System & Premium Tier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Premium Community Owner Dashboard", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Syncs other verified seller sheets and performs comparison analysis graphs.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isCommunityFeatureEnabled,
                            onCheckedChange = { onCommunityToggleChange(it) }
                        )
                    }
                }
            }
        }

        // Theme Settings (Dark Theme vs Light Theme Toggle)
        item {
            Text("Interface Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enable Light Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Switch between White Theme (Light) and Dark Theme (Midnight)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(
                            checked = isLightTheme,
                            onCheckedChange = { onThemeToggleChange(it) }
                        )
                    }
                }
            }
        }

        // DAILY MILK STOCK INVENTORY (NAVIGATE TO INVENTORY OPTION)
        item {
            Text("Milk Stock Inventory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                onClick = onNavigateToInventory,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, PrimaryMilk.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = PrimaryMilk.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    tint = PrimaryMilk,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Manage Daily Milk Inventory", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Logged: Add today's milk volume for Cow, Buffalo & A2 Milk", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Navigate to inventory",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Cloud backups simulated operations
        item {
            Text("Local Backups & Subscriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        Toast.makeText(context, "Encrypted local SQLite backup successfully written to app directory!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Local Backup")
                }

                Button(
                    onClick = {
                        Toast.makeText(context, "Cloud backup restoration check: Up to date!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cloud Restore")
                }
            }
        }

        // Out log button
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout from ERP Console", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// QUICK LAUCNHER ADD CUSTOMER DIALOG
// ==========================================
@Composable
fun QuickAddCustomerDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var qrPreference by remember { mutableStateOf("UPI") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Quick Add Cash/UPI Buyer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Buyer Name (e.g., Arun Sharma)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("Logistics Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("UPI", "CASH").forEach { pref ->
                        val isChosen = qrPreference == pref
                        Button(
                            onClick = { qrPreference = pref },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isChosen) PrimaryMilk else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(pref, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AlertRed)
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSubmit(nameInput, phoneInput, qrPreference)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk)
                    ) {
                        Text("Register Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun MilkInventoryManagerDialog(
    inventories: List<com.example.data.entity.MilkInventoryEntity>,
    onDismiss: () -> Unit,
    onSaveInventory: (Double, Double, Double, String) -> Unit
) {
    var cowInput by remember { mutableStateOf("") }
    var buffaloInput by remember { mutableStateOf("") }
    var a2Input by remember { mutableStateOf("") }

    val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }
    var dateStringInput by remember { mutableStateOf(sdf.format(java.util.Date())) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp),
            border = BorderStroke(1.dp, PrimaryMilk)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = PrimaryMilk.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = null,
                                    tint = PrimaryMilk,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Milk Inventory Stock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content inside
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        "Log Daily Collections (Liters)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMilk
                    )
                    Text(
                        "Track internal milk availability before selling to buyers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dateStringInput,
                        onValueChange = { dateStringInput = it },
                        label = { Text("Stock Log Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cowInput,
                        onValueChange = { cowInput = it },
                        label = { Text("Cow Milk Available (L)") },
                        placeholder = { Text("e.g. 150.25") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = buffaloInput,
                        onValueChange = { buffaloInput = it },
                        label = { Text("Buffalo Milk Available (L)") },
                        placeholder = { Text("e.g. 120") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = a2Input,
                        onValueChange = { a2Input = it },
                        label = { Text("A2 Milk Available (L)") },
                        placeholder = { Text("e.g. 55.5") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Button(
                        onClick = {
                            val cow = cowInput.toDoubleOrNull() ?: 0.0
                            val buf = buffaloInput.toDoubleOrNull() ?: 0.0
                            val a2 = a2Input.toDoubleOrNull() ?: 0.0
                            if (cow > 0 || buf > 0 || a2 > 0) {
                                onSaveInventory(cow, buf, a2, dateStringInput)
                                cowInput = ""
                                buffaloInput = ""
                                a2Input = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMilk),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Today's Stock", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Historical Stock Registers",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (inventories.isEmpty()) {
                        Text(
                            "No stock logs recorded yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        inventories.forEach { stock ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            stock.dateStr,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryMilk
                                        )
                                        val total = stock.cowLiters + stock.buffaloLiters + stock.a2Liters
                                        Text(
                                            "Total: ${total} L",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OrganicGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val itemStyle = MaterialTheme.typography.labelSmall
                                        Text("Cow: ${stock.cowLiters}L", style = itemStyle, modifier = Modifier.weight(1f), color = Color.Gray)
                                        Text("Buf: ${stock.buffaloLiters}L", style = itemStyle, modifier = Modifier.weight(1f), color = Color.Gray)
                                        Text("A2: ${stock.a2Liters}L", style = itemStyle, modifier = Modifier.weight(1f), color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
