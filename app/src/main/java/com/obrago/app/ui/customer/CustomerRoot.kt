package com.obrago.app.ui.customer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.obrago.app.data.model.CommunicationTarget
import com.obrago.app.ui.chat.ChatScreen
import com.obrago.app.ui.chat.ChatViewModel
import com.obrago.app.ui.common.QuitConfirmationDialog

@Composable
fun CustomerRoot(onOpenProfile: () -> Unit) {
    val viewModel: CustomerViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showRatingDialogForJob by remember { mutableStateOf<Pair<String, com.obrago.app.data.model.Bid>?>(null) }
    var showChat by remember { mutableStateOf(false) }
    var counterOfferForBid by remember { mutableStateOf<com.obrago.app.data.model.Bid?>(null) }
    var showQuitDialog by remember { mutableStateOf(false) }

    BackHandler {
        when {
            showChat -> showChat = false
            state.screen == CustomerScreen.POST_JOB -> viewModel.backToHome()
            else -> showQuitDialog = true
        }
    }

    if (showQuitDialog) {
        QuitConfirmationDialog(
            onConfirmQuit = {
                showQuitDialog = false
                (context as? Activity)?.finish()
            },
            onDismiss = { showQuitDialog = false }
        )
    }

    fun openChat(target: CommunicationTarget) {
        chatViewModel.open(target)
        showChat = true
    }

    fun callNumber(phone: String?) {
        val target = phone ?: "03001234567"
        val clean = target.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${clean.ifBlank { "03001234567" }}"))
        context.startActivity(intent)
    }

    if (showChat) {
        ChatScreen(viewModel = chatViewModel, onBack = { showChat = false })
        return
    }

    val activeJob = state.activeJob
    val userJobs = state.currentUser?.let { u -> state.jobs.filter { it.customerId == u.id } } ?: emptyList()

    when {
        activeJob == null -> {
            if (state.screen == CustomerScreen.POST_JOB && state.selectedCategoryId != null) {
                CustomerPostJobScreen(
                    categoryId = state.selectedCategoryId!!,
                    currentUser = state.currentUser,
                    onBack = { viewModel.backToHome() },
                    onSubmit = { description, location, city, coords, budget ->
                        viewModel.postJob(state.selectedCategoryId!!, description, location, city, coords, budget)
                    }
                )
            } else {
                CustomerHomeScreen(
                    currentUser = state.currentUser,
                    userJobs = userJobs,
                    allUsers = state.allUsers,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onOpenProfile = onOpenProfile,
                    onOpenChat = ::openChat
                )
            }
        }

        activeJob.status == "bidding" -> {
            CustomerBiddingScreen(
                job = activeJob,
                bids = viewModel.bidsFor(activeJob),
                customerLocation = activeJob.locationCoords,
                onCancel = { reason -> viewModel.cancelJob(activeJob.id, reason) },
                onAccept = { bid -> viewModel.acceptBid(activeJob.id, bid) },
                onCounter = { bid -> counterOfferForBid = bid }
            )
        }

        else -> {
            val acceptedBid = viewModel.acceptedBidFor(activeJob)
            if (acceptedBid != null) {
                CustomerActiveJobScreen(
                    job = activeJob,
                    acceptedBid = acceptedBid,
                    customerAvatar = state.currentUser?.avatar,
                    customerLocation = activeJob.locationCoords,
                    onOpenChat = ::openChat,
                    onCall = { phone ->
                        val workerUser = state.allUsers.firstOrNull { it.id == acceptedBid.workerId }
                        callNumber(phone ?: workerUser?.phone)
                    },
                    onComplete = {
                        viewModel.completeJob(activeJob.id)
                        showRatingDialogForJob = activeJob.id to acceptedBid
                    },
                    onCancel = { reason -> viewModel.cancelJob(activeJob.id, reason) },
                    onCancelAfterArrival = { viewModel.cancelJobAfterArrival(activeJob, acceptedBid) }
                )
            }
        }
    }

    showRatingDialogForJob?.let { (jobId, bid) ->
        RatingDialog(
            workerName = bid.workerName,
            onDismiss = { showRatingDialogForJob = null },
            onSubmit = { stars, comment ->
                viewModel.submitRating(jobId, bid.workerId, stars, comment)
                showRatingDialogForJob = null
            }
        )
    }

    counterOfferForBid?.let { bid ->
        CounterOfferDialog(
            bid = bid,
            onDismiss = { counterOfferForBid = null },
            onSubmit = { price, note ->
                viewModel.submitCounterOffer(bid.id, price, note)
                counterOfferForBid = null
            }
        )
    }
}
