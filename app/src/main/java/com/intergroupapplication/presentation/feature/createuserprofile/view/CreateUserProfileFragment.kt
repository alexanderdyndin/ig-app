package com.intergroupapplication.presentation.feature.createuserprofile.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.*
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentCreateUserProfileBinding
import com.intergroupapplication.di.qualifier.UserProfileHandler
import com.intergroupapplication.domain.exception.FIRST_NAME
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.SECOND_NAME
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.clicks
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.createuserprofile.presenter.CreateUserProfilePresenter
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.jakewharton.rxbinding3.widget.afterTextChangeEvents
import com.jakewharton.rxbinding3.widget.textChanges
import com.mobsandgeeks.saripaar.QuickRule
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.exceptions.CompositeException
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.time.Year
import java.util.*
import javax.inject.Inject


class CreateUserProfileFragment : BaseFragment(), CreateUserProfileView,
    CompoundButton.OnCheckedChangeListener, Validator.ValidationListener {

    private val viewBinding by viewBinding(FragmentCreateUserProfileBinding::bind)

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
    @UserProfileHandler
    override lateinit var errorHandler: ErrorHandler

    @SuppressLint("NonConstantResourceId")
    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var surName: AppCompatEditText

    @SuppressLint("NonConstantResourceId")
    @NotEmpty(messageResId = R.string.field_should_not_be_empty, trim = true)
    lateinit var name: AppCompatEditText

    @LayoutRes
    override fun layoutRes() = R.layout.fragment_create_user_profile

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
    private lateinit var userCreateAddAvatar: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setErrorHandler()
    }

    override fun viewCreated() {
        userAvatarHolder = viewBinding.userAvatarHolder
        createGroup = viewBinding.createGroup
        man = viewBinding.man
        woman = viewBinding.woman
        progressBar = viewBinding.loader.progressBar
        tvFirstname = viewBinding.tvFirstname
        tvSurname = viewBinding.tvSurname
        inputYear = viewBinding.inputYear
        inputDay = viewBinding.inputDay
        inputMonth = viewBinding.inputMonth
        genderRadioGroup = viewBinding.genderRadioGroup
        userCreateAddAvatar = viewBinding.userCreateAddAvatar
        name = viewBinding.name
        surName = viewBinding.surName

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
        initEditText()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun completed() {
        val activity = requireActivity()
        if (activity is MainActivity) {
            activity.createDrawer()
        } else throw IllegalStateException("Wrong Activity")
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

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        userAvatarHolder.showImageUploadingStarted(chooseMedia)
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        userAvatarHolder.showImageUploaded(chooseMedia)
    }

    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        userAvatarHolder.showImageUploadingProgress(progress, chooseMedia)
    }

    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        userAvatarHolder.showImageUploadingError(chooseMedia)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        buttonView?.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isChecked) R.color.whiteAutorize else R.color.manatee
            )
        )
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
                    R.id.inputDay -> {
                        showErrorMessage(message)
                    }
                    R.id.inputMonth -> {
                        showErrorMessage(message)
                    }
                    R.id.inputYear -> {
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
        Observable.merge(
            name.afterTextChangeEvents(),
            surName.afterTextChangeEvents()
        )
            .subscribe({
                when (it.view.id) {
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
        userCreateAddAvatar.clicks().subscribe { openPhoto() }.also { compositeDisposable.add(it) }
    }

    private fun openPhoto() {
        dialogDelegate.showDialog(
            R.layout.dialog_camera_or_gallery,
            mapOf(R.id.fromCamera to { presenter.takePhotoFromCamera() },
                R.id.fromGallery to { presenter.takePhotoFromGallery() })
        )
    }

    private fun createUserProfile() {
        val gender: String = if (genderRadioGroup.indexOfChild(
                requireView().findViewById
                    (genderRadioGroup.checkedRadioButtonId)
            ) == 0
        ) {
            "male"
        } else {
            "female"
        }
        if (userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
            userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
            userAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR
        ) {
            val birthDay = "${inputDay.text}.${inputMonth.text}.${inputYear.text}"
            presenter.createUserProfile(
                name.text.toString().trim(), surName.text.toString(),
                birthDay, gender
            )
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
                val year = Calendar.getInstance().get(Calendar.YEAR) - 1
                val value = Integer.parseInt(view?.text.toString())
                return value in 1900..year
            }
        })

    }


    private fun initEditText() {
        fun nameListener(view: AppCompatEditText) {
            view.textChanges()
                .subscribe {
                    var textEntered = it.toString()
                    if (textEntered.length == 1 && textEntered[0].isLetter() &&
                        !textEntered[0].isUpperCase()
                    ) {
                        textEntered = textEntered.uppercase()
                        view.setText(textEntered)
                        view.setSelection(view.text?.length ?: 0)
                    }

                    if (textEntered.isNotEmpty() && textEntered.contains(" ")) {
                        textEntered = textEntered.replace(" ", "")
                        view.setText(textEntered)
                        view.setSelection(view.text?.length ?: 0)
                    }
                }.let(compositeDisposable::add)
        }
        nameListener(name)
        nameListener(surName)
    }
}
