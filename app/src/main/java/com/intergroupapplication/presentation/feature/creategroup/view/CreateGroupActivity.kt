package com.intergroupapplication.presentation.feature.creategroup.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
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
import com.mobsandgeeks.saripaar.annotation.Length
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_group_create.*
import kotlinx.android.synthetic.main.auth_loader.progressBar
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import java.util.stream.Stream
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
    lateinit var groupName: EditText

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate

    private var age: String = "12"

    @LayoutRes
    override fun layoutRes() = R.layout.activity_group_create

    override fun getSnackBarCoordinator(): CoordinatorLayout = createGroupCoordinator

    override fun viewCreated() {
        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter<String>(
                this, R.layout.item_autocomplete, R.id.autoCompleteItem, countries
        )

        autoCompleteTextViewCountry.setAdapter(adapter)
        autoCompleteTextViewCountry.threshold = 2

        groupCreate__radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.groupCreate__btnOpen -> {
                    groupCreate__btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_open_btn_act), null, null, null)
                    groupCreate__btnOpen.setTextColor(getColor(R.color.ActiveText))
                    groupCreate__btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_close_btn), null, null, null)
                    groupCreate__btnClose.setTextColor(getColor(R.color.colorPink))
                }
                R.id.groupCreate__btnClose -> {
                    groupCreate__btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_open_btn), null, null, null)
                    groupCreate__btnOpen.setTextColor(getColor(R.color.colorAccent))
                    groupCreate__btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_close_btn_act), null, null, null)
                    groupCreate__btnClose.setTextColor(getColor(R.color.ActiveText))
                }
            }
        }

        groupCreate__subject_btn.setOnClickListener {
            val keyword: String = groupCreate__subject.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(this, "Please enter the keyword!", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val inflater = LayoutInflater.from(this)

                    // Create a Chip from Layout.
                    val newChip = inflater.inflate(R.layout.layout_chip_entry, chipGroupSubject, false) as Chip
                    newChip.text = keyword

                    //
                    // Other methods:
                    //
                    // newChip.setCloseIconVisible(true);
                    // newChip.setCloseIconResource(R.drawable.your_icon);
                    // newChip.setChipIconResource(R.drawable.your_icon);
                    // newChip.setChipBackgroundColorResource(R.color.red);
                    // newChip.setTextAppearanceResource(R.style.ChipTextStyle);
                    // newChip.setElevation(15);
                    chipGroupSubject.addView(newChip)

                    // Set Listener for the Chip:
                    newChip.setOnClickListener {
                        chipGroupSubject.removeView(it)
                    }
                    groupCreate__subject.setText("")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        groupCreate__checkAge.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.groupCreate__btnAge12 -> {
                    groupCreate__btnAge12.setTextColor(getColor(R.color.ActiveText))
                    groupCreate__btnAge16.setTextColor(getColor(R.color.colorAge16))
                    age = "12"
                }
                R.id.groupCreate__btnAge16 -> {
                    groupCreate__btnAge12.setTextColor(getColor(R.color.colorAge12))
                    groupCreate__btnAge16.setTextColor(getColor(R.color.ActiveText))
                    age = "16"
                }
                R.id.groupCreate__btnAge18 -> {
                    groupCreate__btnAge12.setTextColor(getColor(R.color.colorAge12))
                    groupCreate__btnAge16.setTextColor(getColor(R.color.colorAge16))
                    age = "18"
                }
            }
        }

        groupCreate__addAvatar.setOnTouchListener { v, motionEvent ->
            v as TextView
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_addava_act)
                    v.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                }

                MotionEvent.ACTION_MOVE -> {
                }

                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_addava)
                    v.setTextColor(resources.getColor(R.color.colorAccent, this.theme))
                    v.performClick()

                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else ->{

                }
            }
            true
        }
        groupCreate__ageHelp.setOnTouchListener { v, motionEvent ->
            v as TextView
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.bg_agehelp_click)
                    v.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                }

                MotionEvent.ACTION_MOVE -> {
                }

                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.bg_agehelp)
                    v.setTextColor(resources.getColor(R.color.colorAccent, this.theme))
                    v.performClick()

                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else ->{

                }
            }
            true
        }
        groupCreate__ageHelp.setOnClickListener {
            Toast.makeText(this,"12+\n16+\n18+", Toast.LENGTH_LONG).show()
        }
        groupName = findViewById(R.id.groupCreate_title)
        createGroup.setOnClickListener {
            validator.validate()
        }
        groupCreate__addAvatar.setOnClickListener {
            dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
        }
        toolbarBackAction.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        RxTextView.afterTextChangeEvents(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }
        RxView.focusChanges(groupName).subscribe { groupName.error = null }
                .let { { d: Disposable -> compositeDisposable.add(d) } }

//        groupAvatarHolder.imageLoaderDelegate = imageLoaderDelegate
//        groupCreate__radioGroup.setOnCheckedChangeListener { _, _ ->
//                groupCreate__btnOpen.isChecked = !(groupCreate__btnOpen.isChecked)
//                groupCreate__btnClose.isChecked = !(groupCreate__btnClose.isChecked)
//        }
        checkBoxAgreement.setOnCheckedChangeListener { _, b ->
            if (b) {
                createGroup.setBackgroundResource(R.drawable.btn_main_act)
                createGroup.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                createGroup.isEnabled = true
            } else {
                createGroup.setBackgroundResource(R.drawable.btn_main)
                createGroup.setTextColor(resources.getColor(R.color.colorTextBtnNoActive, this.theme))
                createGroup.isEnabled = false
            }
        }
    }

    override fun showImageUploadingStarted(path: String) {
       // groupAvatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded() {
        //groupAvatarHolder.showImageUploaded()
    }

    override fun showImageUploadingProgress(progress: Float) {
        //groupAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
        //groupAvatarHolder.showImageUploadingError()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>) {
        for (error in errors) {
            val view = error.view
            val message = error.getCollatedErrorMessage(this)
            if (view is AppCompatEditText) {
                groupName.error = message
                setViewErrorState(view)
            }
        }
    }

    override fun onValidationSucceeded() {
        //TODO решить вопрос с дизайном аватарки
//        if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
//                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
//                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            presenter.createGroup(groupName.text.toString().trim(),
                    groupCreate__desc.text.toString().trim(), "no theme",
                    groupCreate__rule.text.toString().trim(),groupCreate__btnClose.isChecked, age)
//        } else {
//            dialogDelegate.showErrorSnackBar(getString(R.string.image_still_uploading))
//        }
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
