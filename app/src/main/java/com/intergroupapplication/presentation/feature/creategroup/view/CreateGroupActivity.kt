package com.intergroupapplication.presentation.feature.creategroup.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.setViewErrorState
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.creategroup.presenter.CreateGroupPresenter
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject

class CreateGroupActivity : BaseActivity(), CreateGroupView, Validator.ValidationListener {

    companion object {
        fun getIntent(context: Context?) = Intent(context, CreateGroupActivity::class.java)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateGroupPresenter

    @ProvidePresenter
    fun providePresenter(): CreateGroupPresenter = presenter

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var groupName: AppCompatEditText

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate


    @LayoutRes
    override fun layoutRes() = R.layout.activity_create_group

    override fun getSnackBarCoordinator(): CoordinatorLayout = createGroupCoordinator

    override fun viewCreated() {
        groupName = findViewById(R.id.groupNameInput)
        createGroup.setOnClickListener {
            validator.validate()
        }
        groupAvatarHolder.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
        }
        toolbarBackAction.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        RxTextView.afterTextChangeEvents(groupName).subscribe { groupNameInput.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.focusChanges(groupName).subscribe { groupNameInput.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }
        groupAvatarHolder.imageLoaderDelegate = imageLoaderDelegate
        groupCreate__radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioOpen -> {
                    radioOpen.setButtonDrawable(R.drawable.radio_btn_active)
                    radioClose.setButtonDrawable(R.drawable.radio_btn_noactive)
                }
                R.id.radioClose -> {
                    radioClose.setButtonDrawable(R.drawable.radio_btn_active)
                    radioOpen.setButtonDrawable(R.drawable.radio_btn_noactive)
                }
            }
        }
    }

    override fun showImageUploadingStarted(path: String) {
        groupAvatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded() {
        groupAvatarHolder.showImageUploaded()
    }

    override fun showImageUploadingProgress(progress: Float) {
        groupAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
        groupAvatarHolder.showImageUploadingError()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)
            if (view is AppCompatEditText) {
                groupNameInput.error = message
                setViewErrorState(view)
            }
        }
    }

    override fun onValidationSucceeded() {
        if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            presenter.createGroup(groupName.text.toString().trim(),
                    descriptionGroup.text.toString().trim(), "no theme",
                    rulesGroup.text.toString().trim(),radioClose.isChecked,"16")
        } else {
            dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
        }
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            createGroup.hide()
            progressBar.show()
        } else {
            progressBar.hide()
            createGroup.show()
        }
    }

    private fun loadFromCamera() {
        presenter.takePhotoFromCamera()
    }

    private fun loadFromGallery() {
        presenter.takePhotoFromGallery()
    }
}
