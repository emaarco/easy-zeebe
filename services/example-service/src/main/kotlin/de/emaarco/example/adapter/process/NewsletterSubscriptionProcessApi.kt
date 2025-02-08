package de.emaarco.example.adapter.process

object NewsletterSubscriptionProcessApi {
    val PROCESS_ID: String = "newsletterSubscription"

    object Elements {
        val Timer_EveryDay: String = "Timer_EveryDay"
        val Timer_After3Days: String = "Timer_After3Days"
        val Activity_ConfirmRegistration: String = "Activity_ConfirmRegistration"
        val SubProcess_Confirmation: String = "SubProcess_Confirmation"
        val EndEvent_RegistrationAborted: String = "EndEvent_RegistrationAborted"
        val EndEvent_RegistrationCompleted: String = "EndEvent_RegistrationCompleted"
        val EndEvent_SubscriptionConfirmed: String = "EndEvent_SubscriptionConfirmed"
        val Activity_AbortRegistration: String = "Activity_AbortRegistration"
        val Activity_SendWelcomeMail: String = "Activity_SendWelcomeMail"
        val Activity_SendConfirmationMail: String = "Activity_SendConfirmationMail"
        val StartEvent_SubmitRegistrationForm: String = "StartEvent_SubmitRegistrationForm"
        val StartEvent_RequestReceived: String = "StartEvent_RequestReceived"
    }

    object Messages {
        val Message_FormSubmitted: String = "Message_FormSubmitted"
        val Message_SubscriptionConfirmed: String = "Message_SubscriptionConfirmed"
    }

    object TaskTypes {
        val Activity_AbortRegistration: String = "newsletter.abortRegistration"
        val Activity_SendWelcomeMail: String = "newsletter.sendWelcomeMail"
        val Activity_SendConfirmationMail: String = "newsletter.sendConfirmationMail"
    }
}
