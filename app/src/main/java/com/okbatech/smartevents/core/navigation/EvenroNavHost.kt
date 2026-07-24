package com.okbatech.smartevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.toRoute
import com.okbatech.smartevents.core.designsystem.components.EvenroNavDestination
import com.okbatech.smartevents.feature.auth.presentation.interest.SelectInterestRoute
import com.okbatech.smartevents.feature.auth.presentation.location.SelectLocationRoute
import com.okbatech.smartevents.feature.auth.presentation.resetpassword.ResetPasswordRoute
import com.okbatech.smartevents.feature.auth.presentation.signin.SignInRoute
import com.okbatech.smartevents.feature.auth.presentation.signup.SignUpRoute
import com.okbatech.smartevents.feature.auth.presentation.verification.VerificationRoute
import com.okbatech.smartevents.feature.booking.presentation.addcard.AddNewCardRoute
import com.okbatech.smartevents.feature.booking.presentation.bookeddetails.EventBookedDetailsRoute
import com.okbatech.smartevents.feature.booking.presentation.buyticket.BuyTicketRoute
import com.okbatech.smartevents.feature.booking.presentation.coviddeclaration.CovidDeclarationRoute
import com.okbatech.smartevents.feature.booking.presentation.myevents.MyEventsRoute
import com.okbatech.smartevents.feature.booking.presentation.payment.AddCardResultKey
import com.okbatech.smartevents.feature.booking.presentation.payment.MockCard
import com.okbatech.smartevents.feature.booking.presentation.payment.PaymentConfirmRoute
import com.okbatech.smartevents.feature.booking.presentation.payment.PaymentRoute
import com.okbatech.smartevents.feature.booking.presentation.scancard.ScanCardRoute
import com.okbatech.smartevents.feature.booking.presentation.ticket.TicketRoute
import com.okbatech.smartevents.feature.events.presentation.calendar.CalendarOpenViewRoute
import com.okbatech.smartevents.feature.events.presentation.calendar.CalendarRoute
import com.okbatech.smartevents.feature.events.presentation.details.EventDetailsRoute
import com.okbatech.smartevents.feature.events.presentation.eventform.EventFormRoute
import com.okbatech.smartevents.feature.events.presentation.filter.FilterCriteria
import com.okbatech.smartevents.feature.events.presentation.filter.FilterResultKey
import com.okbatech.smartevents.feature.events.presentation.filter.FilterRoute
import com.okbatech.smartevents.feature.events.presentation.invitefriend.InviteFriendRoute
import com.okbatech.smartevents.feature.events.presentation.reviewpopup.EventReviewPopupRoute
import com.okbatech.smartevents.feature.events.presentation.search.SearchEvent
import com.okbatech.smartevents.feature.events.presentation.search.SearchRoute
import com.okbatech.smartevents.feature.events.presentation.search.SearchViewModel
import com.okbatech.smartevents.feature.events.presentation.seeall.SeeAllEventsRoute
import com.okbatech.smartevents.feature.events.presentation.wishlist.WishlistRoute
import com.okbatech.smartevents.feature.home.presentation.home.HomeRoute
import com.okbatech.smartevents.feature.map.presentation.locationpicker.LocationPickerRoute
import com.okbatech.smartevents.feature.map.presentation.mapview.MapViewRoute
import com.okbatech.smartevents.feature.onboarding.presentation.country.CountrySelectionRoute
import com.okbatech.smartevents.feature.onboarding.presentation.onboarding.OnboardingRoute
import com.okbatech.smartevents.feature.onboarding.presentation.splash.SplashRoute
import com.okbatech.smartevents.feature.profile.presentation.editprofile.EditProfileRoute
import com.okbatech.smartevents.feature.profile.presentation.menu.MenuRoute
import com.okbatech.smartevents.feature.profile.presentation.organizer.OrganizerProfileRoute
import com.okbatech.smartevents.feature.profile.presentation.profile.ProfileRoute
import com.okbatech.smartevents.feature.profile.presentation.share.ShareRoute
import com.okbatech.smartevents.feature.profile.presentation.tooltip.TooltipRoute
import com.okbatech.smartevents.feature.social.presentation.chat.ChatInBookedDetailsRoute
import com.okbatech.smartevents.feature.social.presentation.chat.ChatRoute
import com.okbatech.smartevents.feature.social.presentation.chat.GroupChatRoute
import com.okbatech.smartevents.feature.social.presentation.groupdetails.GroupDetailsRoute
import com.okbatech.smartevents.feature.social.presentation.groupmembers.GroupMembersRoute
import com.okbatech.smartevents.feature.social.presentation.messages.MessagesRoute
import com.okbatech.smartevents.feature.social.presentation.notification.NotificationRoute

@Composable
fun EvenroNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = EvenroRoute.Splash) {

        // ---- Onboarding & auth: real implementations (Phase 1) ----
        composable<EvenroRoute.Splash> {
            SplashRoute(
                onNavigateToOnboarding = {
                    navController.navigate(EvenroRoute.Onboarding()) {
                        popUpTo(EvenroRoute.Splash) { inclusive = true }
                    }
                },
                onNavigateToSignIn = {
                    navController.navigate(EvenroRoute.SignIn) {
                        popUpTo(EvenroRoute.Splash) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(EvenroRoute.Home) {
                        popUpTo(EvenroRoute.Splash) { inclusive = true }
                    }
                },
            )
        }
        composable<EvenroRoute.Onboarding> {
            OnboardingRoute(
                onFinished = {
                    navController.navigate(EvenroRoute.CountrySelection) {
                        popUpTo<EvenroRoute.Onboarding> { inclusive = true }
                    }
                },
            )
        }
        composable<EvenroRoute.CountrySelection> {
            CountrySelectionRoute(
                onBack = navController::popBackStack,
                onSave = {
                    navController.navigate(EvenroRoute.SignIn) {
                        popUpTo<EvenroRoute.CountrySelection> { inclusive = true }
                    }
                },
            )
        }
        composable<EvenroRoute.SignIn> {
            SignInRoute(
                onBack = navController::popBackStack,
                onSignedIn = {
                    navController.navigate(EvenroRoute.Home) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(EvenroRoute.SignUp) },
                onNavigateToResetPassword = { navController.navigate(EvenroRoute.ResetPassword) },
            )
        }
        composable<EvenroRoute.SignUp> {
            SignUpRoute(
                onBack = navController::popBackStack,
                onSignedUp = { email ->
                    navController.navigate(EvenroRoute.Verification(contact = email))
                },
                onNavigateToSignIn = navController::popBackStack,
            )
        }
        composable<EvenroRoute.Verification> { backStackEntry ->
            val route: EvenroRoute.Verification = backStackEntry.toRoute()
            VerificationRoute(
                contact = route.contact,
                onBack = navController::popBackStack,
                onVerified = {
                    navController.navigate(EvenroRoute.SelectInterest) {
                        popUpTo<EvenroRoute.Verification> { inclusive = true }
                    }
                },
            )
        }
        composable<EvenroRoute.ResetPassword> {
            ResetPasswordRoute(
                onBack = navController::popBackStack,
                onRequestSent = navController::popBackStack,
            )
        }
        composable<EvenroRoute.SelectInterest> {
            SelectInterestRoute(
                onSaved = { navController.navigate(EvenroRoute.SelectLocation) },
            )
        }
        composable<EvenroRoute.SelectLocation> {
            SelectLocationRoute(
                onSaved = {
                    navController.navigate(EvenroRoute.Home) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        // ---- Home & discovery ----
        composable<EvenroRoute.Home> {
            HomeRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenSearch = { navController.navigate(EvenroRoute.SearchWhiteBar) },
                onOpenFilter = { navController.navigate(EvenroRoute.Filter) },
                onOpenSeeAll = { navController.navigate(EvenroRoute.SeeAllEvents) },
                onOpenLocationPicker = { navController.navigate(EvenroRoute.LocationPicker) },
                onOpenAddEvent = { navController.navigate(EvenroRoute.AddEvent) },
                onOpenNotifications = { navController.navigate(EvenroRoute.Notification) },
                onOpenMessages = { navController.navigate(EvenroRoute.Message) },
                onNavigate = { destination ->
                    val route = when (destination) {
                        EvenroNavDestination.Home -> null
                        EvenroNavDestination.Calendar -> EvenroRoute.Calendar
                        EvenroNavDestination.Map -> EvenroRoute.MapView
                        EvenroNavDestination.Profile -> EvenroRoute.Profile
                    }
                    if (route != null) {
                        navController.navigate(route) { launchSingleTop = true }
                    }
                },
            )
        }
        composable<EvenroRoute.SeeAllEvents> {
            SeeAllEventsRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
            )
        }
        composable<EvenroRoute.EmptyEvents> {
            MyEventsRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenBookedDetails = { bookingId -> navController.navigate(EvenroRoute.EventBookedDetails(bookingId)) },
            )
        }
        composable<EvenroRoute.SearchWhiteBar> { backStackEntry ->
            val viewModel: SearchViewModel = hiltViewModel(backStackEntry)
            val filterResult = backStackEntry.savedStateHandle.get<FilterCriteria>(FilterResultKey)
            LaunchedEffect(filterResult) {
                if (filterResult != null) {
                    viewModel.onEvent(SearchEvent.FilterApplied(filterResult))
                    backStackEntry.savedStateHandle.remove<FilterCriteria>(FilterResultKey)
                }
            }
            SearchRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenFilter = { navController.navigate(EvenroRoute.Filter) },
                viewModel = viewModel,
            )
        }
        composable<EvenroRoute.SearchColorBar> { backStackEntry ->
            val viewModel: SearchViewModel = hiltViewModel(backStackEntry)
            SearchRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenFilter = { navController.navigate(EvenroRoute.Filter) },
                viewModel = viewModel,
            )
        }
        composable<EvenroRoute.Filter> {
            FilterRoute(
                initialCriteria = FilterCriteria(),
                onBack = navController::popBackStack,
                onApply = { criteria ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(FilterResultKey, criteria)
                    navController.popBackStack()
                },
            )
        }
        composable<EvenroRoute.Calendar> {
            CalendarRoute(
                onBack = navController::popBackStack,
                onOpenDay = { epochDay -> navController.navigate(EvenroRoute.CalendarOpenView(epochDay)) },
            )
        }
        composable<EvenroRoute.CalendarOpenView> {
            CalendarOpenViewRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
            )
        }

        // ---- Map & location ----
        composable<EvenroRoute.LocationPicker> {
            LocationPickerRoute(onSaved = navController::popBackStack)
        }
        composable<EvenroRoute.MapView> {
            MapViewRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onNavigate = { destination ->
                    val route = when (destination) {
                        EvenroNavDestination.Home -> EvenroRoute.Home
                        EvenroNavDestination.Calendar -> EvenroRoute.Calendar
                        EvenroNavDestination.Map -> null
                        EvenroNavDestination.Profile -> EvenroRoute.Profile
                    }
                    if (route != null) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable<EvenroRoute.MapViewV2> {
            MapViewRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onNavigate = { destination ->
                    val route = when (destination) {
                        EvenroNavDestination.Home -> EvenroRoute.Home
                        EvenroNavDestination.Calendar -> EvenroRoute.Calendar
                        EvenroNavDestination.Map -> null
                        EvenroNavDestination.Profile -> EvenroRoute.Profile
                    }
                    if (route != null) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }

        // ---- Events ----
        composable<EvenroRoute.EventPreview> { backStackEntry ->
            val route: EvenroRoute.EventPreview = backStackEntry.toRoute()
            EventDetailsRoute(
                onBack = navController::popBackStack,
                onOpenBuyTicket = { eventId -> navController.navigate(EvenroRoute.BuyTicket(eventId)) },
                onOpenInvite = { eventId -> navController.navigate(EvenroRoute.InviteFriendV1(eventId)) },
                onOpenChat = { threadId, title -> navController.navigate(EvenroRoute.Chat(threadId, title)) },
                onOpenShare = { eventId, title -> navController.navigate(EvenroRoute.Share(eventTitle = title, eventId = eventId)) },
                onOpenOrganizerProfile = { organizerId -> navController.navigate(EvenroRoute.OrganizerProfile(userId = organizerId)) },
            )
        }
        composable<EvenroRoute.EventDetails> {
            EventDetailsRoute(
                onBack = navController::popBackStack,
                onOpenBuyTicket = { eventId -> navController.navigate(EvenroRoute.BuyTicket(eventId)) },
                onOpenInvite = { eventId -> navController.navigate(EvenroRoute.InviteFriendV1(eventId)) },
                onOpenChat = { threadId, title -> navController.navigate(EvenroRoute.Chat(threadId, title)) },
                onOpenShare = { eventId, title -> navController.navigate(EvenroRoute.Share(eventTitle = title, eventId = eventId)) },
                onOpenOrganizerProfile = { organizerId -> navController.navigate(EvenroRoute.OrganizerProfile(userId = organizerId)) },
            )
        }
        composable<EvenroRoute.EventBookedDetails> {
            EventBookedDetailsRoute(
                onBack = navController::popBackStack,
                onOpenChat = { bookingId -> navController.navigate(EvenroRoute.ChatInBookedDetails(bookingId)) },
                onOpenTicket = { bookingId -> navController.navigate(EvenroRoute.Ticket(bookingId)) },
            )
        }
        composable<EvenroRoute.EventPast> {
            MyEventsRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenBookedDetails = { bookingId -> navController.navigate(EvenroRoute.EventBookedDetails(bookingId)) },
            )
        }
        composable<EvenroRoute.EventUpcoming> {
            MyEventsRoute(
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenBookedDetails = { bookingId -> navController.navigate(EvenroRoute.EventBookedDetails(bookingId)) },
            )
        }
        composable<EvenroRoute.WishList> {
            WishlistRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
            )
        }
        composable<EvenroRoute.EventReviewPopup> {
            EventReviewPopupRoute(onDismiss = navController::popBackStack)
        }

        // ---- Organizer authoring ----
        composable<EvenroRoute.AddEvent> {
            EventFormRoute(
                onBack = navController::popBackStack,
                onSaved = { eventId ->
                    navController.navigate(EvenroRoute.EventDetails(eventId)) {
                        popUpTo<EvenroRoute.AddEvent> { inclusive = true }
                    }
                },
            )
        }
        composable<EvenroRoute.EditEvent> {
            EventFormRoute(
                onBack = navController::popBackStack,
                onSaved = { eventId ->
                    navController.navigate(EvenroRoute.EventDetails(eventId)) {
                        popUpTo<EvenroRoute.EditEvent> { inclusive = true }
                    }
                },
            )
        }

        // ---- Booking & payments ----
        composable<EvenroRoute.BuyTicket> {
            BuyTicketRoute(
                onBack = navController::popBackStack,
                onContinue = { eventId, ticketCount ->
                    navController.navigate(EvenroRoute.CovidDeclaration(eventId, ticketCount))
                },
            )
        }
        composable<EvenroRoute.CovidDeclaration> { backStackEntry ->
            val route: EvenroRoute.CovidDeclaration = backStackEntry.toRoute()
            CovidDeclarationRoute(
                onBack = navController::popBackStack,
                onContinue = { navController.navigate(EvenroRoute.Payment(route.eventId, route.ticketCount)) },
            )
        }
        composable<EvenroRoute.Payment> { backStackEntry ->
            val newCard = backStackEntry.savedStateHandle.get<MockCard>(AddCardResultKey)
            PaymentRoute(
                newCard = newCard,
                onNewCardConsumed = { backStackEntry.savedStateHandle.remove<MockCard>(AddCardResultKey) },
                onBack = navController::popBackStack,
                onOpenAddCard = { navController.navigate(EvenroRoute.AddNewCard) },
                onPaid = { bookingId ->
                    navController.navigate(EvenroRoute.Ticket(bookingId)) {
                        popUpTo(EvenroRoute.Home) { inclusive = false }
                    }
                },
            )
        }
        composable<EvenroRoute.PaymentConfirm> {
            PaymentConfirmRoute(
                onBackToHome = {
                    navController.navigate(EvenroRoute.Home) { popUpTo(EvenroRoute.Home) { inclusive = true } }
                },
            )
        }
        composable<EvenroRoute.AddNewCard> { backStackEntry ->
            val scannedNumber = backStackEntry.savedStateHandle.get<String>("scanned_card_number")
            AddNewCardRoute(
                scannedNumber = scannedNumber,
                onBack = navController::popBackStack,
                onOpenScanCard = { navController.navigate(EvenroRoute.ScanCard) },
                onSaved = { card ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(AddCardResultKey, card)
                    navController.popBackStack()
                },
            )
        }
        composable<EvenroRoute.ScanCard> {
            ScanCardRoute(
                onCardScanned = { number ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_card_number", number)
                    navController.popBackStack()
                },
            )
        }
        composable<EvenroRoute.Ticket> {
            TicketRoute(
                onClose = { navController.popBackStack(EvenroRoute.Home, inclusive = false) },
            )
        }
        composable<EvenroRoute.InviteFriendV1> {
            InviteFriendRoute(onBack = navController::popBackStack)
        }
        composable<EvenroRoute.InviteFriendV2> {
            InviteFriendRoute(onBack = navController::popBackStack)
        }

        // ---- Social ----
        composable<EvenroRoute.Notification> {
            NotificationRoute(onBack = navController::popBackStack)
        }
        composable<EvenroRoute.EmptyNotification> {
            NotificationRoute(onBack = navController::popBackStack)
        }
        composable<EvenroRoute.Message> {
            MessagesRoute(
                onBack = navController::popBackStack,
                onOpenChat = { threadId, title -> navController.navigate(EvenroRoute.Chat(threadId, title)) },
                onOpenGroupChat = { eventId -> navController.navigate(EvenroRoute.GroupChat(eventId)) },
            )
        }
        composable<EvenroRoute.Chat> {
            ChatRoute(onBack = navController::popBackStack)
        }
        composable<EvenroRoute.ChatInBookedDetails> {
            ChatInBookedDetailsRoute(onBack = navController::popBackStack)
        }
        composable<EvenroRoute.GroupChat> {
            GroupChatRoute(
                onBack = navController::popBackStack,
                onOpenGroupDetails = { eventId -> navController.navigate(EvenroRoute.GroupDetails(eventId)) },
            )
        }
        composable<EvenroRoute.GroupDetails> {
            GroupDetailsRoute(
                onBack = navController::popBackStack,
                onOpenGroupChat = { eventId -> navController.navigate(EvenroRoute.GroupChat(eventId)) },
                onOpenGroupMembers = { eventId -> navController.navigate(EvenroRoute.GroupMembers(eventId)) },
            )
        }
        composable<EvenroRoute.GroupMembers> {
            GroupMembersRoute(onBack = navController::popBackStack)
        }

        // ---- Profile ----
        composable<EvenroRoute.Profile> {
            ProfileRoute(
                onOpenEditProfile = { navController.navigate(EvenroRoute.EditProfile) },
                onOpenOrganizerProfile = { userId -> navController.navigate(EvenroRoute.OrganizerProfile(userId = userId)) },
                onOpenMyEvents = { navController.navigate(EvenroRoute.EmptyEvents) },
                onOpenWishlist = { navController.navigate(EvenroRoute.WishList) },
                onOpenNotifications = { navController.navigate(EvenroRoute.Notification) },
                onOpenMessages = { navController.navigate(EvenroRoute.Message) },
                onOpenMenu = { navController.navigate(EvenroRoute.MenuV1) },
                onSignedOut = {
                    navController.navigate(EvenroRoute.SignIn) { popUpTo(navController.graph.id) { inclusive = true } }
                },
                onNavigate = { destination ->
                    val route = when (destination) {
                        EvenroNavDestination.Home -> EvenroRoute.Home
                        EvenroNavDestination.Calendar -> EvenroRoute.Calendar
                        EvenroNavDestination.Map -> EvenroRoute.MapView
                        EvenroNavDestination.Profile -> null
                    }
                    if (route != null) navController.navigate(route) { launchSingleTop = true }
                },
            )
        }
        composable<EvenroRoute.EditProfile> {
            EditProfileRoute(
                onBack = navController::popBackStack,
                onSaved = navController::popBackStack,
            )
        }
        composable<EvenroRoute.OrganizerProfile> {
            OrganizerProfileRoute(
                onBack = navController::popBackStack,
                onOpenEventDetails = { eventId -> navController.navigate(EvenroRoute.EventDetails(eventId)) },
                onOpenEditProfile = { navController.navigate(EvenroRoute.EditProfile) },
            )
        }

        // ---- Chrome / overlays ----
        composable<EvenroRoute.MenuV1> {
            MenuRoute(
                onBack = navController::popBackStack,
                onOpenProfile = navController::popBackStack,
                onOpenEditProfile = { navController.navigate(EvenroRoute.EditProfile) },
                onOpenMyEvents = { navController.navigate(EvenroRoute.EmptyEvents) },
                onOpenWishlist = { navController.navigate(EvenroRoute.WishList) },
                onOpenNotifications = { navController.navigate(EvenroRoute.Notification) },
                onOpenMessages = { navController.navigate(EvenroRoute.Message) },
                onSignedOut = {
                    navController.navigate(EvenroRoute.SignIn) { popUpTo(navController.graph.id) { inclusive = true } }
                },
            )
        }
        composable<EvenroRoute.MenuV2> {
            MenuRoute(
                onBack = navController::popBackStack,
                onOpenProfile = navController::popBackStack,
                onOpenEditProfile = { navController.navigate(EvenroRoute.EditProfile) },
                onOpenMyEvents = { navController.navigate(EvenroRoute.EmptyEvents) },
                onOpenWishlist = { navController.navigate(EvenroRoute.WishList) },
                onOpenNotifications = { navController.navigate(EvenroRoute.Notification) },
                onOpenMessages = { navController.navigate(EvenroRoute.Message) },
                onSignedOut = {
                    navController.navigate(EvenroRoute.SignIn) { popUpTo(navController.graph.id) { inclusive = true } }
                },
            )
        }
        composable<EvenroRoute.ToolTip> {
            TooltipRoute(message = "Tap here to create your first event", onDismiss = navController::popBackStack)
        }
        composable<EvenroRoute.ToolTipV2> {
            TooltipRoute(message = "Swipe categories to explore more events", onDismiss = navController::popBackStack)
        }
        composable<EvenroRoute.Share> { backStackEntry ->
            val route: EvenroRoute.Share = backStackEntry.toRoute()
            ShareRoute(
                eventTitle = route.eventTitle,
                eventId = route.eventId,
                onDismiss = navController::popBackStack,
            )
        }
    }
}
