package com.intergroupapplication.presentation.feature.creategroup.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
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
import kotlinx.android.synthetic.main.activity_group_create.*
import kotlinx.android.synthetic.main.auth_loader.*
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
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
    lateinit var groupName: EditText

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var validator: Validator

    @Inject
    lateinit var imageLoaderDelegate: ImageLoadingDelegate

    private var age: String = "12"
    private var subjects: MutableList<String> = mutableListOf<String>()

    @LayoutRes
    override fun layoutRes() = R.layout.activity_group_create

    override fun getSnackBarCoordinator(): CoordinatorLayout = createGroupCoordinator

    override fun viewCreated() {
        val countries = resources.getStringArray(R.array.countries)
        val adapter = ArrayAdapter<String>(
                this, R.layout.item_autocomplete, R.id.autoCompleteItem, countries
        )
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        autoCompleteTextViewCountry.setAdapter(adapter)
        autoCompleteTextViewCountry.threshold = 2

        groupCreate__desc.setOnClickListener {
            it.visibility = View.GONE
            groupCreate__descContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            groupCreate__descContainer.performClick()
        }

        groupCreate__descEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                groupCreate__desc.visibility = View.VISIBLE
                groupCreate__descContainer.visibility = View.GONE
            }
        }

        groupCreate__rule.setOnClickListener {
            it.visibility = View.GONE
            groupCreate__ruleContainer.visibility = View.VISIBLE
            hideKeyboard(imm)
            groupCreate__ruleContainer.performClick()
        }

        groupCreate__ruleEdit.setOnFocusChangeListener { _, b ->
            if (!b) {
                groupCreate__rule.visibility = View.VISIBLE
                groupCreate__ruleContainer.visibility = View.GONE
            }
        }
        groupCreate__ruleContainer.setOnClickListener {
            groupCreate__ruleEdit.requestFocus()
            showKeyboard(imm)
        }
        groupCreate__descContainer.setOnClickListener {
            groupCreate__descEdit.requestFocus()
            showKeyboard(imm)
        }


        groupCreate__radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.groupCreate_btnOpen -> {
                    groupCreate_btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_open_btn_act), null, null, null)
                    groupCreate_btnOpen.setTextColor(getColor(R.color.ActiveText))
                    groupCreate_btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_close_btn), null, null, null)
                    groupCreate_btnClose.setTextColor(getColor(R.color.colorPink))
                }
                R.id.groupCreate_btnClose -> {
                    groupCreate_btnOpen.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_open_btn), null, null, null)
                    groupCreate_btnOpen.setTextColor(getColor(R.color.colorAccent))
                    groupCreate_btnClose.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_close_btn_act), null, null, null)
                    groupCreate_btnClose.setTextColor(getColor(R.color.ActiveText))
                }
            }
        }

        groupCreate__subject_btn.setOnClickListener {
            val keyword: String = groupCreate__subject.text.toString()
            if (keyword.isEmpty()) {
                Toast.makeText(this, R.string.empty_subject_error, Toast.LENGTH_SHORT).show()
            } else if (subjects.size>=5) {
                Toast.makeText(this, R.string.full_subject_error, Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val inflater = LayoutInflater.from(this)
                    val newChip = inflater.inflate(R.layout.layout_chip_entry, chipGroupSubject, false) as Chip
                    newChip.text = keyword
                    subjects.add(keyword)
                    chipGroupSubject.addView(newChip)
                    newChip.setOnCloseIconClickListener {
                        subjects.remove((it as Chip).text)
                        chipGroupSubject.removeView(it)
                        groupCreate__lineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                    }
                    groupCreate__subject.setText("")
                    groupCreate__lineInput4.changeSeparatorBackground(subjects.isNotEmpty())
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: " + e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        groupCreate__subject_btn.setOnTouchListener { v, motionEvent ->
            v as TextView
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this@CreateGroupActivity, R.drawable.btn_plus_act), null, null, null)
                }

                MotionEvent.ACTION_MOVE -> {
                }

                MotionEvent.ACTION_UP -> {
                    v.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this@CreateGroupActivity, R.drawable.btn_plus), null, null, null)
                    v.performClick()

                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else ->{

                }
            }
            true
        }

        groupCreate__checkAge.setOnCheckedChangeListener { _, i ->
            groupCreate__lineAge.setBackgroundResource(R.drawable.line_input_act)
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
                    v.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_plus_addava_act), null, null, null)
                    v.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                }

                MotionEvent.ACTION_MOVE -> {
                }

                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_addava)
                    v.setTextColor(resources.getColor(R.color.colorAccent, this.theme))
                    v.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_plus_addava), null, null, null)
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
            Toast.makeText(this, "12+\n16+\n18+", Toast.LENGTH_LONG).show()
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

        groupAvatarHolder.imageLoaderDelegate = imageLoaderDelegate
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

        createGroup.setOnTouchListener { v, motionEvent ->
            v as TextView
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.setBackgroundResource(R.drawable.btn_main_press)
                    v.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                }

                MotionEvent.ACTION_MOVE -> {
                }

                MotionEvent.ACTION_UP -> {
                    v.setBackgroundResource(R.drawable.btn_main_act)
                    v.setTextColor(resources.getColor(R.color.ActiveText, this.theme))
                    v.performClick()

                }

                MotionEvent.ACTION_CANCEL -> {

                }

                else ->{

                }
            }
            true
        }
        groupName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    groupCreate__lineOpenClose.setBackgroundResource(R.drawable.line_openclose_act)
                    groupCreate__lineAge.setBackgroundResource(R.drawable.line_input_act)
                } else {
                    groupCreate__lineOpenClose.setBackgroundResource(R.drawable.line_openclose)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCountry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput2.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextViewLang.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                groupCreate__lineInput3.changeSeparatorBackground(s.isNotEmpty())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })


        groupCreate_btnOpen.isChecked = true
        groupCreate__btnAge12.isChecked = true
    }

    override fun showImageUploadingStarted(path: String) {
       groupAvatarHolder.showImageUploadingStarted(path)
    }

    override fun showImageUploaded() {
        groupAvatarHolder.showImageUploaded()
        groupCreate_line_r.setBackgroundResource(R.drawable.line_addava_act)
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
                groupName.error = message
                setViewErrorState(view)
            }
        }
    }

    override fun onValidationSucceeded() {
        if (groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE ||
                groupAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.ERROR) {
            presenter.createGroup(groupName.text.toString().trim(),
                    groupCreate__desc.text.toString().trim(), "no theme",
                    groupCreate__rule.text.toString().trim(), groupCreate_btnClose.isChecked, age)
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

    fun View.setMarginTop(topMargin: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin, topMargin, params.rightMargin, params.bottomMargin)
        layoutParams = params
    }

    fun View.setMarginBottom(bottomMargin: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
        layoutParams = params
    }

    fun View.changeSeparatorBackground(active: Boolean) {
        if (active) {
            setBackgroundResource(R.drawable.line_input_act)
        } else {
            setBackgroundResource(R.drawable.line_input)
        }
    }

    private fun hideKeyboard(imm: InputMethodManager) {
        val view: View? = this.currentFocus
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showKeyboard(imm: InputMethodManager) {
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

}
