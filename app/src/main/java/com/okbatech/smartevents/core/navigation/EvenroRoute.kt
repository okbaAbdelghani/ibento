package com.okbatech.smartevents.core.navigation

import kotlinx.serialization.Serializable

/**
 * Every screen in the Evenro design, as type-safe Navigation-Compose routes.
 * Grouped by feature area to mirror the `feature/<name>` package layout. Screens not
 * yet implemented render [com.okbatech.smartevents.core.navigation.PlaceholderScreen]
 * so the whole app stays click-through from Phase 0 onward.
 */
sealed interface EvenroRoute {

    // ---- Onboarding (01-05) ----
    @Serializable data object Splash : EvenroRoute
    @Serializable data class Onboarding(val page: Int = 1) : EvenroRoute
    @Serializable data object CountrySelection : EvenroRoute

    // ---- Auth (06-11) ----
    @Serializable data object SignIn : EvenroRoute
    @Serializable data object SignUp : EvenroRoute
    @Serializable data class Verification(val contact: String) : EvenroRoute
    @Serializable data object ResetPassword : EvenroRoute
    @Serializable data object SelectInterest : EvenroRoute
    @Serializable data object SelectLocation : EvenroRoute

    // ---- Home & discovery (12, 16, 24-28) ----
    @Serializable data object Home : EvenroRoute
    @Serializable data object SeeAllEvents : EvenroRoute
    @Serializable data object EmptyEvents : EvenroRoute
    @Serializable data object SearchWhiteBar : EvenroRoute
    @Serializable data object SearchColorBar : EvenroRoute
    @Serializable data object Filter : EvenroRoute
    @Serializable data object Calendar : EvenroRoute
    @Serializable data class CalendarOpenView(val epochDay: Long) : EvenroRoute

    // ---- Map & location (21-23) ----
    @Serializable data object LocationPicker : EvenroRoute
    @Serializable data object MapView : EvenroRoute
    @Serializable data object MapViewV2 : EvenroRoute

    // ---- Events (13-15, 17-20) ----
    @Serializable data class EventPreview(val eventId: String) : EvenroRoute
    @Serializable data class EventDetails(val eventId: String) : EvenroRoute
    @Serializable data class EventBookedDetails(val bookingId: String) : EvenroRoute
    @Serializable data object EventPast : EvenroRoute
    @Serializable data object EventUpcoming : EvenroRoute
    @Serializable data object WishList : EvenroRoute
    @Serializable data class EventReviewPopup(val eventId: String) : EvenroRoute

    // ---- Organizer authoring (29-30) ----
    @Serializable data object AddEvent : EvenroRoute
    @Serializable data class EditEvent(val eventId: String) : EvenroRoute

    // ---- Booking & payments (39-45) ----
    @Serializable data class BuyTicket(val eventId: String) : EvenroRoute
    @Serializable data class CovidDeclaration(val eventId: String, val ticketCount: Int) : EvenroRoute
    @Serializable data class Payment(val eventId: String, val ticketCount: Int) : EvenroRoute
    @Serializable data class PaymentConfirm(val eventId: String) : EvenroRoute
    @Serializable data object AddNewCard : EvenroRoute
    @Serializable data object ScanCard : EvenroRoute
    @Serializable data class Ticket(val bookingId: String) : EvenroRoute
    @Serializable data class InviteFriendV1(val eventId: String) : EvenroRoute
    @Serializable data class InviteFriendV2(val eventId: String) : EvenroRoute

    // ---- Social (31-38) ----
    @Serializable data object Notification : EvenroRoute
    @Serializable data object EmptyNotification : EvenroRoute
    @Serializable data object Message : EvenroRoute
    @Serializable data class Chat(val threadId: String, val title: String) : EvenroRoute
    @Serializable data class ChatInBookedDetails(val bookingId: String) : EvenroRoute
    @Serializable data class GroupChat(val groupId: String) : EvenroRoute
    @Serializable data class GroupDetails(val groupId: String) : EvenroRoute
    @Serializable data class GroupMembers(val groupId: String) : EvenroRoute

    // ---- Profile (48-52) ----
    @Serializable data object Profile : EvenroRoute
    @Serializable data object EditProfile : EvenroRoute
    @Serializable data class OrganizerProfile(val userId: String, val tab: OrganizerTab = OrganizerTab.About) : EvenroRoute

    // ---- Chrome / overlays (53-59) ----
    @Serializable data object MenuV1 : EvenroRoute
    @Serializable data object MenuV2 : EvenroRoute
    @Serializable data object ToolTip : EvenroRoute
    @Serializable data object ToolTipV2 : EvenroRoute
    @Serializable data class Share(val eventTitle: String = "this event", val eventId: String = "") : EvenroRoute
}

@Serializable
enum class OrganizerTab { About, Events, Reviews }
