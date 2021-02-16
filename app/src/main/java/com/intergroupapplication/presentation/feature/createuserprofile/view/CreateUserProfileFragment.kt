package com.intergroupapplication.presentation.feature.createuserprofile.view

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.exception.FIRST_NAME
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.SECOND_NAME
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.createuserprofile.presenter.CreateUserProfilePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobsandgeeks.saripaar.QuickRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import kotlinx.android.synthetic.main.fragment_create_user_profile.*
import kotlinx.android.synthetic.main.auth_loader.*

import javax.inject.Inject


class CreateUserProfileFragment : BaseFragment(), CreateUserProfileView,
        CompoundButton.OnCheckedChangeListener, Validator.ValidationListener {

    companion object {
        fun getIntent(context: Context?) = Intent(context, CreateUserProfileFragment::class.java)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateUserProfilePresenter

    @ProvidePresenter
    fun providePresenter(): CreateUserProfilePresenter = presenter
//
//    @Inject
//    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var colorStateList: ColorStateList

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate

    @Inject
    lateinit var validator: Validator

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var surName: AppCompatEditText

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var name: AppCompatEditText

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_user_profile

    override fun getSnackBarCoordinator(): CoordinatorLayout = createUserCoordinator

    override fun viewCreated() {
//        setSupportActionBar(toolbar)
//        supportActionBar?.apply {
//            setDisplayHomeAsUpEnabled(true)
//            setHomeButtonEnabled(true)
//            setTitle(R.string.editor_profile)
//        }

        name = requireView().findViewById(R.id.name)
        surName = requireView().findViewById(R.id.surName)

        CompoundButtonCompat.setButtonTintList(man, colorStateList)
        CompoundButtonCompat.setButtonTintList(woman, colorStateList)
        listenEditTexts()
        listenGenderToggle()
        listenAvatarClicks()
        nextCreate.clicks()
                .subscribe { validator.validate() }
                .also { compositeDisposable.add(it) }
        initRadioButton()
        initValidator()
        setErrorHandler()
        initEditText()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun completed() {
        findNavController().navigate(R.id.action_createUserProfileActivity_to_splashActivity)
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            nextCreate.hide()
            progressBar.show()
        } else {
            nextCreate.show()
            progressBar.hide()
        }
    }

    override fun showImageUploadingStarted(path: String) {
        avatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded(path: String) {
        avatarHolder.showImageUploaded()
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        avatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError(path: String) {
        avatarHolder.showImageUploadingError()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        buttonView?.setTextColor(ContextCompat.getColor(requireContext(),
                if (isChecked) R.color.whiteAutorize else R.color.manatee))
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(requireContext())
            if (view is AppCompatEditText) {
                when (view.id) {
                    R.id.name -> {
                        tvFirstname.text = message
                        tvFirstname.show()
                    }
                    R.id.surName -> {
                        tvSurname.text = message
                        tvSurname.show()
                    }
                    R.id.etDD -> {
                        showErrorMessage(message)
                    }
                    R.id.etMM -> {
                        showErrorMessage(message)
                    }
                    R.id.etGGGG -> {
                        showErrorMessage(message)
                    }
                }
            }
        }
    }

    override fun onValidationSucceeded() {
        createUserProfile()
    }

    private fun listenEditTexts() {
        Observable.merge(RxTextView.afterTextChangeEvents(name),
                RxTextView.afterTextChangeEvents(surName))
                .subscribe({
                    when (it.view().id) {
                        R.id.surName -> tvSurname.gone()
                        R.id.name -> tvFirstname.gone()
                    }
                }, { it.printStackTrace() }).let { compositeDisposable.add(it) }

    }

    private fun listenGenderToggle() {
        genderRadioGroup.setOnCheckedChangeListener { _, _ ->
            nextCreate.show()
        }
    }

    private fun listenAvatarClicks() {
        avatarHolder.clicks().subscribe { openPhoto() }.also { compositeDisposable.add(it) }
        change.setLinkClickable { openPhoto() }
    }

    private fun openPhoto() {
        dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                mapOf(R.id.fromCamera to { presenter.takePhotoFromCamera() },
                        R.id.fromGallery to { presenter.takePhotoFromGallery() }))
    }

    private fun createUserProfile() {
        val gender: String = if (genderRadioGroup.indexOfChild(requireView().findViewById
                (genderRadioGroup.checkedRadioButtonId)) == 0) {
            "male"
        } else {
            "female"
        }
        if (avatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
                avatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
                avatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            val birthDay = "${etDD.text}.${etMM.text}.${etGGGG.text}"
            presenter.createUserProfile(name.text.toString().trim(), surName.text.toString(),
                    birthDay, gender)
        } else {
            dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
        }
    }

    private fun initRadioButton() {
        man.setOnCheckedChangeListener(this)
        woman.setOnCheckedChangeListener(this)
        man.isChecked = true
    }

    private fun setErrorHandler() {
        errorHandler.on(CompositeException::class.java) { throwable, _ ->
            run {
                (throwable as? CompositeException)?.exceptions?.forEach { ex ->
                    (ex as? FieldException)?.let {
                        if (it.field == FIRST_NAME) {
                            tvFirstname.text = it.message
                            tvFirstname.show()
                        } else if (it.field == SECOND_NAME) {
                            tvSurname.text = it.message
                            tvSurname.show()
                        }
                    }
                }
            }
        }
    }

    private fun initValidator() {

        validator.put(etDD, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    "Неправильная дата"

            override fun isValid(view: TextView?): Boolean {
                if (view?.text.toString().isEmpty()) return false
                val value = Integer.parseInt(view?.text.toString())
                return value in 1..31
            }
        })

        validator.put(etMM, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    "Неправильная дата"

            override fun isValid(view: TextView?): Boolean {
                if (view?.text.toString().isEmpty()) return false
                val value = Integer.parseInt(view?.text.toString())
                return value in 1..12
            }
        })

        validator.put(etGGGG, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    "Неправильная дата"

            override fun isValid(view: TextView?): Boolean {
                if (view?.text.toString().isEmpty()) return false
                val value = Integer.parseInt(view?.text.toString())
                return value in 1..2100
            }
        })

    }

    private fun initEditText() {

        name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var textEntered = s.toString()

                if (textEntered.length == 1 && textEntered[0].isLetter() && !textEntered[0].isUpperCase()) {
                    textEntered = s?.toString().orEmpty().toUpperCase()
                    name.setText(textEntered)
                    name.setSelection(name.text?.length ?: 0)
                }

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    textEntered = s.toString().replace(" ", "")
                    name.setText(textEntered)
                    name.setSelection(name.text?.length ?: 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

        surName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                var textEntered = s.toString()

                if (textEntered.length == 1 && textEntered[0].isLetter() && !textEntered[0].isUpperCase()) {
                    textEntered = s?.toString().orEmpty().toUpperCase()
                    surName.setText(textEntered)
                    surName.setSelection(surName.text?.length ?: 0)
                }

                if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                    textEntered = s.toString().replace(" ", "")
                    surName.setText(textEntered)
                    surName.setSelection(surName.text?.length ?: 0)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })

    }
}