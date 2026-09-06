package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffer

@Composable
fun PersonalizedOfferRow(
    offers: List<PersonalizedOffer>,
    onDisplayed: (String) -> Unit,
    onTapped: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(offers, key = { it.id }) { offer ->
            PersonalizedOfferCard(
                offer = offer,
                onDisplayed = { onDisplayed(offer.id) },
                onTapped = { onTapped(offer.id) }
            )
        }
    }
}

@Composable
fun PersonalizedOfferCard(
    offer: PersonalizedOffer,
    onDisplayed: () -> Unit,
    onTapped: () -> Unit
) {
    LaunchedEffect(offer.id) { onDisplayed() }

    ElevatedCard(
        onClick = onTapped,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(230.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (!offer.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = offer.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (offer.title.isNotBlank()) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (offer.body.isNotBlank()) {
                Text(
                    text = offer.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
