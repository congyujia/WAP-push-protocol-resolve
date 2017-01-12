##===========WAP push PDU resolve for android============


WAP PDU is broadcasted by android framework, APP with receiver can receive it and resolve PDU data to notify user.
to realize this purpose, a receiver with the permission `android.permission.BROADCAST_WAP_PUSH` and intent-filter with action `android.provider.Telephony.WAP_PUSH_DELIVER` should be declared.
