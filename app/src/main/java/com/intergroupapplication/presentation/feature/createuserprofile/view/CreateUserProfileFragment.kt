package com.intergroupapplication.presentation.feature.createuserprofile.view

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentCreateUserProfile2Binding
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
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException

import javax.inject.Inject
import javax.inject.Named


class CreateUserProfileFragment : BaseFragment(), CreateUserProfileView,
        CompoundButton.OnCheckedChangeListener, Validator.ValidationListener {

    companion object {
        fun getIntent(context: Context?) = Intent(context, CreateUserProfileFragment::class.java)
    }

    private val viewBinding by viewBinding(FragmentCreateUserProfile2Binding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateUserProfilePresenter

    @ProvidePresenter
    fun providePresenter(): CreateUserProfilePresenter = presenter

    @Inject
    lateinit var colorStateList: ColorStateList

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate

    @Inject
    lateinit var validator: Validator

    @Inject
    @Named("userProfileHandler")
    lateinit var errorHandlerLogin: ErrorHandler

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var surName: AppCompatEditText

    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var name: AppCompatEditText

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_user_profile2

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.createUserCoordinator

    private lateinit var userAvatarHolder: AvatarImageUploadingView
    private lateinit var createGroup: Button
    private lateinit var man: RadioButton
    private lateinit var woman: RadioButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvFirstname: AppCompatTextView
    private lateinit var tvSurname: AppCompatTextView
    private lateinit var inputDay: AppCompatEditText
    private lateinit var inputMonth: AppCompatEditText
    private lateinit var inputYear: AppCompatEditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var userCreate__addAvatar: TextView

    override fun viewCreated() {
        userAvatarHolder = viewBinding.userAvatarHolder
        createGroup = viewBinding.createGroup
        man = viewBinding.man
        woman = viewBinding.woman
        progressBar = viewBinding.loader.progressBar
        tvFirstname = viewBinding.tvFirstname
        tvSurname = viewBinding.tvSurname
        inputDay = viewBinding.inputDay
        inputMonth = viewBinding.inputMonth
        genderRadioGroup = viewBinding.genderRadioGroup
        userCreate__addAvatar = viewBinding.userCreateAddAvatar

        initErrorHandler(errorHandler)
        name = requireView().findViewById(R.id.name)
        surName = requireView().findViewById(R.id.surName)

        userAvatarHolder.imageLoaderDelegate = imageLoaderDelegate

        CompoundButtonCompat.setButtonTintList(man, colorStateList)
        CompoundButtonCompat.setButtonTintList(woman, colorStateList)
        listenEditTexts()
        listenGenderToggle()
        listenAvatarClicks()
        createGroup.clicks()
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
            createGroup.hide()
            progressBar.show()
        } else {
            createGroup.show()
            progressBar.hide()
        }
    }

    override fun showImageUploadingStarted(path: String) {
        userAvatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded(path: String) {
        userAvatarHolder.showImageUploaded(path)
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        userAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError(path: String) {
        userAvatarHolder.showImageUploadingError(path)
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
            createGroup.show()
        }
    }

    private fun listenAvatarClicks() {
        userCreate__addAvatar.clicks().subscribe { openPhoto() }.also { compositeDisposable.add(it) }
//        change.setLinkClickable { openPhoto() }
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
        if (userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
            userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
            userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            val birthDay = "${inputDay.text}.${inputMonth.text}.${inputYear.text}"
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
        errorHandlerLogin.on(CompositeException::class.java) { throwable, _ ->
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

        validator.put(inputDay, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    "Неправильная дата"

            override fun isValid(view: TextView?): Boolean {
                if (view?.text.toString().isEmpty()) return false
                val value = Integer.parseInt(view?.text.toString())
                return value in 1..31
            }
        })

        validator.put(inputMonth, object : QuickRule<TextView>() {

            override fun getMessage(context: Context?) =
                    "Неправильная дата"

            override fun isValid(view: TextView?): Boolean {
                if (view?.text.toString().isEmpty()) return false
                val value = Integer.parseInt(view?.text.toString())
                return value in 1..12
            }
        })

        validator.put(inputYear, object : QuickRule<TextView>() {

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