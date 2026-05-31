package com.project.lmrs.config;

import com.project.lmrs.entity.*;
import com.project.lmrs.enums.*;
import com.project.lmrs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ReservationRepository reservationRepository;
    private final FolioRepository folioRepository;
    private final FolioLineItemRepository folioLineItemRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableReservationRepository tableReservationRepository;
    private final PartnerAccountRepository partnerAccountRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_TENANT_SUBDOMAIN = "grand-horizon";

    @Override
    public void run(String... args) {
        Tenant tenant = findOrCreateTenant();
        seedUsers(tenant);
        seedRoomTypes(tenant);
        seedGuests(tenant);
        seedMenu(tenant);
        seedTables(tenant);
        seedInventory(tenant);
        seedPartnerAccounts(tenant);
        seedOperationalData(tenant);
    }

    private Tenant findOrCreateTenant() {
        return tenantRepository.findBySubdomain(DEFAULT_TENANT_SUBDOMAIN)
                .orElseGet(() -> {
                    log.info("Creating default tenant...");
                    return tenantRepository.save(Tenant.builder()
                            .name("Grand Horizon Hotel & Residency")
                            .subdomain(DEFAULT_TENANT_SUBDOMAIN)
                            .configJson(Map.of("timezone", "Asia/Kolkata", "currency", "INR"))
                            .isActive(true)
                            .build());
                });
    }

    private void seedUsers(Tenant tenant) {
        Map<String, UserRole> seedUsers = Map.of(
                "admin@lmrs.com", UserRole.SUPER_ADMIN,
                "manager@lmrs.com", UserRole.MANAGER,
                "frontdesk@lmrs.com", UserRole.FRONT_DESK
        );

        Map<String, String> passwords = Map.of(
                "admin@lmrs.com", "admin123",
                "manager@lmrs.com", "manager123",
                "frontdesk@lmrs.com", "front123"
        );

        for (var entry : seedUsers.entrySet()) {
            String email = entry.getKey();
            if (userRepository.findByEmail(email).isEmpty()) {
                userRepository.save(User.builder()
                        .tenant(tenant)
                        .email(email)
                        .passwordHash(passwordEncoder.encode(passwords.get(email)))
                        .role(entry.getValue())
                        .isActive(true)
                        .build());
                log.info("  Created user: {} / {}", email, passwords.get(email));
            }
        }
    }

    private void seedRoomTypes(Tenant tenant) {
        Set<String> existing = roomTypeRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(RoomType::getName).collect(Collectors.toSet());

        if (!existing.contains("Standard")) {
            roomTypeRepository.save(RoomType.builder()
                    .tenant(tenant).name("Standard").maxOccupancy(2)
                    .description("Comfortable room with essential amenities")
                    .amenities(List.of("WiFi", "TV", "Mini Fridge", "Tea Maker")).build());
        }
        if (!existing.contains("Deluxe")) {
            roomTypeRepository.save(RoomType.builder()
                    .tenant(tenant).name("Deluxe").maxOccupancy(3)
                    .description("Spacious room with premium amenities and city view")
                    .amenities(List.of("WiFi", "TV", "Mini Bar", "City View", "Bathtub", "Tea Maker")).build());
        }
        if (!existing.contains("Suite")) {
            roomTypeRepository.save(RoomType.builder()
                    .tenant(tenant).name("Suite").maxOccupancy(4)
                    .description("Luxury suite with separate living area and butler service")
                    .amenities(List.of("WiFi", "TV", "Mini Bar", "Garden View", "Living Room", "Butler Service", "Jacuzzi")).build());
        }

        seedRooms(tenant);
    }

    private void seedRooms(Tenant tenant) {
        List<RoomType> types = roomTypeRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId());
        RoomType standard = types.stream().filter(t -> "Standard".equals(t.getName())).findFirst().orElse(null);
        RoomType deluxe = types.stream().filter(t -> "Deluxe".equals(t.getName())).findFirst().orElse(null);
        RoomType suite = types.stream().filter(t -> "Suite".equals(t.getName())).findFirst().orElse(null);

        Set<String> existingRoomNumbers = roomRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(Room::getRoomNumber).collect(Collectors.toSet());

        for (int floor = 1; floor <= 3; floor++) {
            for (int roomNum = 1; roomNum <= 5; roomNum++) {
                String number = String.format("%d%02d", floor, roomNum);
                if (existingRoomNumbers.contains(number)) continue;

                RoomType type;
                BigDecimal rate;
                if (roomNum <= 2) {
                    type = standard;
                    rate = new BigDecimal("2500.00");
                } else if (roomNum <= 4) {
                    type = deluxe;
                    rate = new BigDecimal("4500.00");
                } else {
                    type = suite;
                    rate = new BigDecimal("7500.00");
                }

                roomRepository.save(Room.builder()
                        .tenant(tenant).roomNumber(number).roomType(type)
                        .floor(floor).status(RoomStatus.AVAILABLE).baseRate(rate)
                        .build());
            }
        }
    }

    private void seedGuests(Tenant tenant) {
        Set<String> existingEmails = guestRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(Guest::getEmail).collect(Collectors.toSet());

        List<Guest> indianGuests = List.of(
                Guest.builder().tenant(tenant).firstName("Aarav").lastName("Sharma")
                        .email("aarav.sharma@email.com").phone("+91-9876543210").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Priya").lastName("Verma")
                        .email("priya.verma@email.com").phone("+91-9876543211").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Rohit").lastName("Patel")
                        .email("rohit.patel@email.com").phone("+91-9876543212").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Ananya").lastName("Gupta")
                        .email("ananya.gupta@email.com").phone("+91-9876543213").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Vikram").lastName("Singh")
                        .email("vikram.singh@email.com").phone("+91-9876543214").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Neha").lastName("Reddy")
                        .email("neha.reddy@email.com").phone("+91-9876543215").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Arjun").lastName("Nair")
                        .email("arjun.nair@email.com").phone("+91-9876543216").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Kavya").lastName("Joshi")
                        .email("kavya.joshi@email.com").phone("+91-9876543217").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Rahul").lastName("Deshmukh")
                        .email("rahul.deshmukh@email.com").phone("+91-9876543218").nationality("IN").build(),
                Guest.builder().tenant(tenant).firstName("Isha").lastName("Malhotra")
                        .email("isha.malhotra@email.com").phone("+91-9876543219").nationality("IN").build()
        );

        for (Guest guest : indianGuests) {
            if (!existingEmails.contains(guest.getEmail())) {
                guestRepository.save(guest);
                log.info("  Created guest: {} {}", guest.getFirstName(), guest.getLastName());
            }
        }
    }

    private void seedMenu(Tenant tenant) {
        Set<String> existingCategories = menuCategoryRepository.findAllByTenant_TenantIdAndIsDeletedFalseOrderByDisplayOrderAsc(tenant.getTenantId())
                .stream().map(MenuCategory::getName).collect(Collectors.toSet());

        MenuCategory mains = null;
        MenuCategory starters = null;
        MenuCategory desserts = null;
        MenuCategory beverages = null;

        if (!existingCategories.contains("Main Course")) {
            mains = menuCategoryRepository.save(MenuCategory.builder()
                    .tenant(tenant).name("Main Course").displayOrder(1).isActive(true).build());
        } else {
            mains = menuCategoryRepository.findByNameAndTenant_TenantId("Main Course", tenant.getTenantId());
        }

        if (!existingCategories.contains("Starters")) {
            starters = menuCategoryRepository.save(MenuCategory.builder()
                    .tenant(tenant).name("Starters").displayOrder(2).isActive(true).build());
        } else {
            starters = menuCategoryRepository.findByNameAndTenant_TenantId("Starters", tenant.getTenantId());
        }

        if (!existingCategories.contains("Desserts")) {
            desserts = menuCategoryRepository.save(MenuCategory.builder()
                    .tenant(tenant).name("Desserts").displayOrder(3).isActive(true).build());
        } else {
            desserts = menuCategoryRepository.findByNameAndTenant_TenantId("Desserts", tenant.getTenantId());
        }

        if (!existingCategories.contains("Beverages")) {
            beverages = menuCategoryRepository.save(MenuCategory.builder()
                    .tenant(tenant).name("Beverages").displayOrder(4).isActive(true).build());
        } else {
            beverages = menuCategoryRepository.findByNameAndTenant_TenantId("Beverages", tenant.getTenantId());
        }

        seedMenuItems(tenant, mains, starters, desserts, beverages);
    }

    private MenuItem findMenuItem(Tenant tenant, MenuCategory category, String name) {
        return menuItemRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream()
                .filter(m -> m.getCategory().getCategoryId().equals(category.getCategoryId()))
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void seedMenuItems(Tenant tenant, MenuCategory mains, MenuCategory starters,
                                MenuCategory desserts, MenuCategory beverages) {
        if (findMenuItem(tenant, mains, "Butter Chicken") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(mains).name("Butter Chicken")
                    .description("Tender chicken in creamy tomato gravy with butter and aromatic spices, served with naan")
                    .basePrice(new BigDecimal("450.00")).allergens(List.of("Dairy", "Gluten"))
                    .dietaryFlags(List.of()).isAvailable(true).build());
        }
        if (findMenuItem(tenant, mains, "Chicken Biryani") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(mains).name("Chicken Biryani")
                    .description("Fragrant basmati rice layered with spiced chicken, caramelized onions, and saffron")
                    .basePrice(new BigDecimal("380.00")).allergens(List.of("Gluten"))
                    .dietaryFlags(List.of()).isAvailable(true).build());
        }
        if (findMenuItem(tenant, starters, "Samosa") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(starters).name("Samosa")
                    .description("Crispy golden pastry filled with spiced potato and peas, served with mint chutney")
                    .basePrice(new BigDecimal("120.00")).allergens(List.of("Gluten"))
                    .dietaryFlags(List.of("Vegetarian")).isAvailable(true).build());
        }
        if (findMenuItem(tenant, starters, "Paneer Tikka") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(starters).name("Paneer Tikka")
                    .description("Grilled cottage cheese marinated in yogurt and aromatic spices with bell peppers")
                    .basePrice(new BigDecimal("250.00")).allergens(List.of("Dairy"))
                    .dietaryFlags(List.of("Vegetarian", "Gluten-Free")).isAvailable(true).build());
        }
        if (findMenuItem(tenant, desserts, "Gulab Jamun") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(desserts).name("Gulab Jamun")
                    .description("Deep-fried milk solid dumplings soaked in rose-flavored sugar syrup")
                    .basePrice(new BigDecimal("150.00")).allergens(List.of("Dairy", "Gluten"))
                    .dietaryFlags(List.of("Vegetarian")).isAvailable(true).build());
        }
        if (findMenuItem(tenant, beverages, "Masala Chai") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(beverages).name("Masala Chai")
                    .description("Traditional Indian spiced tea brewed with cardamom, ginger, cinnamon, and cloves")
                    .basePrice(new BigDecimal("80.00")).allergens(List.of("Dairy"))
                    .dietaryFlags(List.of("Vegetarian", "Gluten-Free")).isAvailable(true).build());
        }
        if (findMenuItem(tenant, beverages, "Mango Lassi") == null) {
            menuItemRepository.save(MenuItem.builder()
                    .tenant(tenant).category(beverages).name("Mango Lassi")
                    .description("Refreshing yogurt drink blended with ripe Alphonso mango pulp and a hint of cardamom")
                    .basePrice(new BigDecimal("120.00")).allergens(List.of("Dairy"))
                    .dietaryFlags(List.of("Vegetarian", "Gluten-Free")).isAvailable(true).build());
        }
    }

    private void seedTables(Tenant tenant) {
        Set<String> existingTableNumbers = restaurantTableRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(RestaurantTable::getTableNumber).collect(Collectors.toSet());

        for (int i = 1; i <= 8; i++) {
            String tableNum = "T" + (100 + i);
            if (existingTableNumbers.contains(tableNum)) continue;

            String zone = i <= 4 ? "Main Hall" : "Terrace";
            restaurantTableRepository.save(RestaurantTable.builder()
                    .tenant(tenant).tableNumber(tableNum).zone(zone)
                    .capacity(i % 2 == 0 ? 4 : 2).status("AVAILABLE")
                    .positionX(BigDecimal.valueOf((i - 1) % 4 * 100 + 50))
                    .positionY(BigDecimal.valueOf(i <= 4 ? 100 : 300))
                    .build());
        }
    }

    private void seedInventory(Tenant tenant) {
        Set<String> existing = inventoryItemRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(InventoryItem::getName).collect(Collectors.toSet());

        record InventorySeed(String name, String unit, double stock, double threshold, double cost) {}

        List<InventorySeed> items = List.of(
                // Kitchen & Spices
                new InventorySeed("Turmeric Powder", "kg", 5.0, 1.0, 280.00),
                new InventorySeed("Cumin Seeds", "kg", 4.0, 1.0, 180.00),
                new InventorySeed("Cardamom", "kg", 2.0, 0.5, 1200.00),
                new InventorySeed("Cinnamon Sticks", "kg", 3.0, 0.5, 450.00),
                new InventorySeed("Cloves", "kg", 1.5, 0.5, 600.00),
                new InventorySeed("Coriander Powder", "kg", 5.0, 1.0, 160.00),
                new InventorySeed("Red Chili Powder", "kg", 4.0, 1.0, 240.00),
                new InventorySeed("Garam Masala", "kg", 3.0, 0.5, 320.00),
                new InventorySeed("Basmati Rice", "kg", 25.0, 5.0, 140.00),
                new InventorySeed("Wheat Flour (Atta)", "kg", 20.0, 5.0, 38.00),
                // Lodge & Housekeeping
                new InventorySeed("Bath Towel", "pieces", 80.0, 15.0, 350.00),
                new InventorySeed("Hand Towel", "pieces", 60.0, 10.0, 180.00),
                new InventorySeed("Fitted Bedsheet (Queen)", "pieces", 40.0, 8.0, 550.00),
                new InventorySeed("Pillow Cover", "pieces", 60.0, 12.0, 120.00),
                new InventorySeed("Toilet Paper Roll", "pieces", 200.0, 40.0, 45.00),
                new InventorySeed("Hand Soap", "pieces", 100.0, 20.0, 35.00),
                new InventorySeed("Shampoo Sachet", "pieces", 300.0, 50.0, 8.00),
                new InventorySeed("Disposable Slippers", "pairs", 80.0, 20.0, 95.00),
                new InventorySeed("Laundry Detergent", "kg", 15.0, 3.0, 280.00),
                new InventorySeed("Room Freshener", "liters", 8.0, 2.0, 190.00),
                // Fresh Produce & Protein
                new InventorySeed("Chicken (Boneless)", "kg", 15.0, 4.0, 240.00),
                new InventorySeed("Chicken (Curry Cut)", "kg", 20.0, 5.0, 200.00),
                new InventorySeed("Paneer (Cottage Cheese)", "kg", 10.0, 2.0, 320.00),
                new InventorySeed("Fresh Milk", "liters", 30.0, 8.0, 56.00),
                new InventorySeed("Curd (Yogurt)", "kg", 12.0, 3.0, 80.00),
                new InventorySeed("Tomatoes", "kg", 15.0, 4.0, 40.00),
                new InventorySeed("Onions", "kg", 25.0, 5.0, 30.00),
                new InventorySeed("Potatoes", "kg", 30.0, 6.0, 25.00),
                new InventorySeed("Green Chilies", "kg", 3.0, 0.5, 60.00),
                new InventorySeed("Fresh Coriander", "kg", 2.0, 0.5, 80.00)
        );

        for (InventorySeed item : items) {
            if (!existing.contains(item.name())) {
                inventoryItemRepository.save(InventoryItem.builder()
                        .tenant(tenant)
                        .name(item.name())
                        .unit(item.unit())
                        .currentStock(BigDecimal.valueOf(item.stock()))
                        .reorderThreshold(BigDecimal.valueOf(item.threshold()))
                        .costPerUnit(BigDecimal.valueOf(item.cost()))
                        .build());
                log.info("  Created inventory item: {}", item.name());
            }
        }
    }

    private void seedPartnerAccounts(Tenant tenant) {
        Set<String> existing = partnerAccountRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId())
                .stream().map(PartnerAccount::getName).collect(Collectors.toSet());

        if (!existing.contains("Booking.com Partner")) {
            partnerAccountRepository.save(PartnerAccount.builder()
                    .tenant(tenant).name("Booking.com Partner")
                    .providerType("BOOKING_COM").apiKey("lrms_partner_booking_com_001")
                    .isActive(true).build());
            log.info("  Created partner: Booking.com Partner");
        }
        if (!existing.contains("Expedia Partner")) {
            partnerAccountRepository.save(PartnerAccount.builder()
                    .tenant(tenant).name("Expedia Partner")
                    .providerType("EXPEDIA").apiKey("lrms_partner_expedia_001")
                    .isActive(true).build());
            log.info("  Created partner: Expedia Partner");
        }
        if (!existing.contains("Direct Connect Partner")) {
            partnerAccountRepository.save(PartnerAccount.builder()
                    .tenant(tenant).name("Direct Connect Partner")
                    .providerType("CUSTOM").apiKey("lrms_partner_direct_001")
                    .isActive(true).build());
            log.info("  Created partner: Direct Connect Partner");
        }
    }

    private void seedOperationalData(Tenant tenant) {
        if (reservationRepository.countByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId()) > 0) {
            log.info("Operational data already seeded, skipping");
            return;
        }
        log.info("Seeding operational data...");

        List<Guest> guests = guestRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId());
        List<Room> rooms = roomRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId());
        List<MenuItem> menuItems = menuItemRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId());
        List<RestaurantTable> tables = restaurantTableRepository.findAllByTenant_TenantIdAndIsDeletedFalse(tenant.getTenantId());

        Map<String, Guest> guestByName = new HashMap<>();
        for (Guest gst : guests) guestByName.put(gst.getFirstName() + " " + gst.getLastName(), gst);

        Map<String, Room> roomByNum = new HashMap<>();
        for (Room rm : rooms) roomByNum.put(rm.getRoomNumber(), rm);

        Map<String, MenuItem> menuByName = new HashMap<>();
        for (MenuItem mi : menuItems) menuByName.put(mi.getName(), mi);

        Map<String, RestaurantTable> tableByNum = new HashMap<>();
        for (RestaurantTable tb : tables) tableByNum.put(tb.getTableNumber(), tb);

        LocalDate today = LocalDate.now();

        Guest aarav = guestByName.get("Aarav Sharma");
        Guest priya = guestByName.get("Priya Verma");
        Guest rohit = guestByName.get("Rohit Patel");
        Guest ananya = guestByName.get("Ananya Gupta");
        Guest vikram = guestByName.get("Vikram Singh");
        Guest neha = guestByName.get("Neha Reddy");
        Guest arjun = guestByName.get("Arjun Nair");
        Guest kavya = guestByName.get("Kavya Joshi");

        Room r101 = roomByNum.get("101");
        Room r102 = roomByNum.get("102");
        Room r103 = roomByNum.get("103");
        Room r201 = roomByNum.get("201");
        Room r203 = roomByNum.get("203");
        Room r204 = roomByNum.get("204");
        Room r301 = roomByNum.get("301");
        Room r305 = roomByNum.get("305");

        BigDecimal stdRate = new BigDecimal("2500.00");
        BigDecimal delRate = new BigDecimal("4500.00");
        BigDecimal suiRate = new BigDecimal("7500.00");

        // ==================== RESERVATIONS ====================

        Reservation r1 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(aarav).room(r101)
                .checkInDate(today.minusDays(5)).checkOutDate(today.minusDays(2))
                .status(ReservationStatus.CHECKED_OUT).channel(BookingChannel.DIRECT)
                .rateApplied(stdRate).build());

        Reservation r2 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(priya).room(r201)
                .checkInDate(today.minusDays(4)).checkOutDate(today.minusDays(2))
                .status(ReservationStatus.CHECKED_OUT).channel(BookingChannel.BOOKING_COM)
                .rateApplied(delRate).build());

        Reservation r3 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(rohit).room(r301)
                .checkInDate(today.minusDays(9)).checkOutDate(today.minusDays(4))
                .status(ReservationStatus.CHECKED_OUT).channel(BookingChannel.EXPEDIA)
                .rateApplied(suiRate).build());

        Reservation r4 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(ananya).room(r203)
                .checkInDate(today.plusDays(1)).checkOutDate(today.plusDays(4))
                .status(ReservationStatus.CONFIRMED).channel(BookingChannel.DIRECT)
                .rateApplied(delRate).build());

        Reservation r5 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(vikram).room(r102)
                .checkInDate(today.minusDays(2)).checkOutDate(today.plusDays(1))
                .status(ReservationStatus.CHECKED_IN).channel(BookingChannel.BOOKING_COM)
                .rateApplied(stdRate).build());

        Reservation r6 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(neha).room(r305)
                .checkInDate(today.minusDays(7)).checkOutDate(today.minusDays(3))
                .status(ReservationStatus.CHECKED_OUT).channel(BookingChannel.DIRECT)
                .rateApplied(suiRate).build());

        Reservation r7 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(arjun).room(r103)
                .checkInDate(today.plusDays(3)).checkOutDate(today.plusDays(5))
                .status(ReservationStatus.CONFIRMED).channel(BookingChannel.PHONE)
                .rateApplied(stdRate).build());

        Reservation r8 = reservationRepository.save(Reservation.builder()
                .tenant(tenant).guest(kavya).room(r204)
                .checkInDate(today.minusDays(1)).checkOutDate(today)
                .status(ReservationStatus.CHECKED_IN).channel(BookingChannel.WALK_IN)
                .rateApplied(delRate).build());

        log.info("  Created 8 reservations (3 CHECKED_OUT, 2 CHECKED_IN, 2 CONFIRMED)");

        // ==================== ROOM STATUS UPDATES ====================

        r102.setStatus(RoomStatus.OCCUPIED);
        r204.setStatus(RoomStatus.OCCUPIED);
        r203.setStatus(RoomStatus.RESERVED);
        r103.setStatus(RoomStatus.RESERVED);
        roomRepository.save(r102);
        roomRepository.save(r204);
        roomRepository.save(r203);
        roomRepository.save(r103);
        log.info("  Updated room statuses (2 OCCUPIED, 2 RESERVED)");

        // ==================== FOLIOS, LINE ITEMS & PAYMENTS ====================

        // Folio 1 — Aarav (CHECKED_OUT): Room ₹7,500 + Food ₹500 + Tax ₹1,000 = ₹9,000
        BigDecimal f1Total = new BigDecimal("9000.00");
        Folio f1 = folioRepository.save(Folio.builder().reservation(r1).guest(aarav)
                .status("CLOSED").currency("INR").totalAmount(f1Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f1)
                .description("Room Charge - Standard 101 (3 nights @ ₹2,500)")
                .amount(new BigDecimal("7500.00")).chargeType(ChargeType.ROOM_CHARGE).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f1)
                .description("Room Service - Butter Chicken & Naan")
                .amount(new BigDecimal("500.00")).chargeType(ChargeType.RESTAURANT).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f1)
                .description("GST 18%").amount(new BigDecimal("1000.00")).chargeType(ChargeType.TAX).build());
        paymentRepository.save(Payment.builder().folio(f1).amount(f1Total).currency("INR")
                .method(PaymentMethod.CARD).gatewayRef("TXN001001").status("COMPLETED")
                .paidAt(LocalDateTime.now().minusDays(2)).build());

        // Folio 2 — Priya (CHECKED_OUT): Room ₹9,000 + Food ₹600 + Tax ₹1,400 = ₹11,000
        BigDecimal f2Total = new BigDecimal("11000.00");
        Folio f2 = folioRepository.save(Folio.builder().reservation(r2).guest(priya)
                .status("CLOSED").currency("INR").totalAmount(f2Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f2)
                .description("Room Charge - Deluxe 201 (2 nights @ ₹4,500)")
                .amount(new BigDecimal("9000.00")).chargeType(ChargeType.ROOM_CHARGE).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f2)
                .description("Restaurant - Samosa & Masala Chai").amount(new BigDecimal("600.00"))
                .chargeType(ChargeType.RESTAURANT).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f2)
                .description("GST 18%").amount(new BigDecimal("1400.00")).chargeType(ChargeType.TAX).build());
        paymentRepository.save(Payment.builder().folio(f2).amount(f2Total).currency("INR")
                .method(PaymentMethod.CARD).gatewayRef("TXN001002").status("COMPLETED")
                .paidAt(LocalDateTime.now().minusDays(2)).build());

        // Folio 3 — Rohit (CHECKED_OUT): Room ₹37,500 + Food ₹1,200 + Tax ₹3,300 = ₹42,000
        BigDecimal f3Total = new BigDecimal("42000.00");
        Folio f3 = folioRepository.save(Folio.builder().reservation(r3).guest(rohit)
                .status("CLOSED").currency("INR").totalAmount(f3Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f3)
                .description("Room Charge - Suite 301 (5 nights @ ₹7,500)")
                .amount(new BigDecimal("37500.00")).chargeType(ChargeType.ROOM_CHARGE).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f3)
                .description("Restaurant - Biryani, Paneer Tikka & Lassi")
                .amount(new BigDecimal("1200.00")).chargeType(ChargeType.RESTAURANT).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f3)
                .description("GST 18%").amount(new BigDecimal("3300.00")).chargeType(ChargeType.TAX).build());
        paymentRepository.save(Payment.builder().folio(f3).amount(f3Total).currency("INR")
                .method(PaymentMethod.CARD).gatewayRef("TXN001003").status("COMPLETED")
                .paidAt(LocalDateTime.now().minusDays(4)).build());

        // Folio 4 — Neha (CHECKED_OUT): Room ₹30,000 + Food ₹900 + Tax ₹2,100 = ₹33,000
        BigDecimal f4Total = new BigDecimal("33000.00");
        Folio f4 = folioRepository.save(Folio.builder().reservation(r6).guest(neha)
                .status("CLOSED").currency("INR").totalAmount(f4Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f4)
                .description("Room Charge - Suite 305 (4 nights @ ₹7,500)")
                .amount(new BigDecimal("30000.00")).chargeType(ChargeType.ROOM_CHARGE).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f4)
                .description("Minibar - Premium Snacks & Drinks")
                .amount(new BigDecimal("900.00")).chargeType(ChargeType.MINIBAR).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f4)
                .description("GST 18%").amount(new BigDecimal("2100.00")).chargeType(ChargeType.TAX).build());
        paymentRepository.save(Payment.builder().folio(f4).amount(f4Total).currency("INR")
                .method(PaymentMethod.MOBILE_WALLET).gatewayRef("UPI001004").status("COMPLETED")
                .paidAt(LocalDateTime.now().minusDays(3)).build());

        // Folio 5 — Vikram (CHECKED_IN, partial payment): Room ₹5,000 (2 of 3 nights) + Food ₹400 + Tax ₹600 = ₹6,000
        BigDecimal f5Total = new BigDecimal("6000.00");
        Folio f5 = folioRepository.save(Folio.builder().reservation(r5).guest(vikram)
                .status("OPEN").currency("INR").totalAmount(f5Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f5)
                .description("Room Charge - Standard 102 (2 nights @ ₹2,500)")
                .amount(new BigDecimal("5000.00")).chargeType(ChargeType.ROOM_CHARGE).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f5)
                .description("Restaurant - Chicken Biryani & Masala Chai")
                .amount(new BigDecimal("400.00")).chargeType(ChargeType.RESTAURANT).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f5)
                .description("GST 18%").amount(new BigDecimal("600.00")).chargeType(ChargeType.TAX).build());
        paymentRepository.save(Payment.builder().folio(f5).amount(new BigDecimal("5000.00")).currency("INR")
                .method(PaymentMethod.CASH).gatewayRef("CASH001005").status("COMPLETED")
                .paidAt(LocalDateTime.now().minusDays(1)).build());

        // Folio 6 — Kavya (CHECKED_IN, no payment yet): Room ₹4,500
        BigDecimal f6Total = new BigDecimal("4500.00");
        Folio f6 = folioRepository.save(Folio.builder().reservation(r8).guest(kavya)
                .status("OPEN").currency("INR").totalAmount(f6Total).build());
        folioLineItemRepository.save(FolioLineItem.builder().folio(f6)
                .description("Room Charge - Deluxe 204 (1 night @ ₹4,500)")
                .amount(new BigDecimal("4500.00")).chargeType(ChargeType.ROOM_CHARGE).build());

        log.info("  Created 6 folios with line items and 5 completed payments");

        // ==================== RESTAURANT ORDERS ====================

        MenuItem butterChicken = menuByName.get("Butter Chicken");
        MenuItem biryani = menuByName.get("Chicken Biryani");
        MenuItem samosa = menuByName.get("Samosa");
        MenuItem paneerTikka = menuByName.get("Paneer Tikka");
        MenuItem gulabJamun = menuByName.get("Gulab Jamun");
        MenuItem masalaChai = menuByName.get("Masala Chai");
        MenuItem mangoLassi = menuByName.get("Mango Lassi");

        RestaurantTable t101 = tableByNum.get("T101");
        RestaurantTable t102 = tableByNum.get("T102");
        RestaurantTable t103 = tableByNum.get("T103");
        RestaurantTable t106 = tableByNum.get("T106");

        // Order 1 — T101, CLOSED: Butter Chicken x1 (₹450) + Masala Chai x2 (₹160) = ₹610
        BigDecimal o1Total = new BigDecimal("610.00");
        Order o1 = orderRepository.save(Order.builder().tenant(tenant).table(t101).guest(aarav)
                .status(OrderStatus.CLOSED).totalAmount(o1Total).build());
        orderItemRepository.save(OrderItem.builder().order(o1).menuItem(butterChicken)
                .quantity(1).unitPrice(new BigDecimal("450.00")).status(OrderItemStatus.DONE).build());
        orderItemRepository.save(OrderItem.builder().order(o1).menuItem(masalaChai)
                .quantity(2).unitPrice(new BigDecimal("80.00")).status(OrderItemStatus.DONE).build());

        // Order 2 — T103, CLOSED: Chicken Biryani x1 (₹380) + Mango Lassi x1 (₹120) = ₹500
        BigDecimal o2Total = new BigDecimal("500.00");
        Order o2 = orderRepository.save(Order.builder().tenant(tenant).table(t103).guest(priya)
                .status(OrderStatus.CLOSED).totalAmount(o2Total).build());
        orderItemRepository.save(OrderItem.builder().order(o2).menuItem(biryani)
                .quantity(1).unitPrice(new BigDecimal("380.00")).status(OrderItemStatus.DONE).build());
        orderItemRepository.save(OrderItem.builder().order(o2).menuItem(mangoLassi)
                .quantity(1).unitPrice(new BigDecimal("120.00")).status(OrderItemStatus.DONE).build());

        // Order 3 — T106, SERVED: Paneer Tikka x1 (₹250) + Samosa x2 (₹240) + Gulab Jamun x2 (₹300) = ₹790
        BigDecimal o3Total = new BigDecimal("790.00");
        Order o3 = orderRepository.save(Order.builder().tenant(tenant).table(t106).guest(neha)
                .status(OrderStatus.SERVED).totalAmount(o3Total).build());
        orderItemRepository.save(OrderItem.builder().order(o3).menuItem(paneerTikka)
                .quantity(1).unitPrice(new BigDecimal("250.00")).status(OrderItemStatus.DONE).build());
        orderItemRepository.save(OrderItem.builder().order(o3).menuItem(samosa)
                .quantity(2).unitPrice(new BigDecimal("120.00")).status(OrderItemStatus.DONE).build());
        orderItemRepository.save(OrderItem.builder().order(o3).menuItem(gulabJamun)
                .quantity(2).unitPrice(new BigDecimal("150.00")).status(OrderItemStatus.DONE).build());

        // Order 4 — T102, OPEN: Butter Chicken x2 (₹900) + Biryani x1 (₹380) + Masala Chai x3 (₹240) = ₹1,520
        BigDecimal o4Total = new BigDecimal("1520.00");
        Order o4 = orderRepository.save(Order.builder().tenant(tenant).table(t102).guest(rohit)
                .status(OrderStatus.OPEN).totalAmount(o4Total).build());
        orderItemRepository.save(OrderItem.builder().order(o4).menuItem(butterChicken)
                .quantity(2).unitPrice(new BigDecimal("450.00")).status(OrderItemStatus.PENDING).build());
        orderItemRepository.save(OrderItem.builder().order(o4).menuItem(biryani)
                .quantity(1).unitPrice(new BigDecimal("380.00")).status(OrderItemStatus.PENDING).build());
        orderItemRepository.save(OrderItem.builder().order(o4).menuItem(masalaChai)
                .quantity(3).unitPrice(new BigDecimal("80.00")).status(OrderItemStatus.PENDING).build());

        log.info("  Created 4 restaurant orders with order items");

        // ==================== TABLE RESERVATIONS ====================

        tableReservationRepository.save(TableReservation.builder().tenant(tenant)
                .guest(rohit).table(t102).partySize(4)
                .reservationDt(LocalDateTime.now().withHour(19).withMinute(0).withSecond(0))
                .status("CONFIRMED").specialNotes("Anniversary dinner - prefer quiet area").build());

        tableReservationRepository.save(TableReservation.builder().tenant(tenant)
                .guest(vikram).table(tableByNum.get("T105")).partySize(2)
                .reservationDt(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0))
                .status("PENDING").specialNotes("Business meeting").build());

        tableReservationRepository.save(TableReservation.builder().tenant(tenant)
                .guest(ananya).table(tableByNum.get("T107")).partySize(6)
                .reservationDt(LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0))
                .status("CONFIRMED").specialNotes("Birthday celebration - need cake arrangement").build());

        log.info("  Created 3 table reservations");
        log.info("Operational data seeding complete: ₹1,00,000+ total revenue, 2 occupied rooms, 4 orders");
    }
}
