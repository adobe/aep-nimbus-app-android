package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.ConsentState

interface ConsentService {
    fun update(state: ConsentState)
}