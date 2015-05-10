package com.scepticallistic.kevin.tipcalc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Outline;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.Collections;

public class CalculatorFragment extends Fragment implements AdapterView.OnItemSelectedListener, PercentDialogFragment.PercentDialogListener {
    private static final String LOG_TAG = CalculatorFragment.class.getSimpleName();
    public SharedPreferences preferences;
    public SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    public Activity main;
    public View rootView, scroll_view, calculator_card, sale_text, tip_text, total_text, fab_plus,
            split_card, people_count, split_tip, split_total, even_content, uneven_content, sales_tax;
    public TableLayout people_layout;
    private static final ArrayList<String> percentsArray = new ArrayList<>();
    private static final ArrayList<String> defaultPercentsArray = new ArrayList<>();
    private static final ArrayList<String> toSpinnerPercentsArray = new ArrayList<>();
    private static final ArrayList<Person> peopleList = new ArrayList<>();
    public AppCompatSpinner spinner;
    public ArrayAdapter<String> adapter;
    public String prefUnitKey, prefPeopleKey, prefPercentsKey, prefDefaultKey, unitSymbol, peoplePref,
            percentString, addPercentString, defaultPercent, prefTaxKey;
    public double sale, percent, tip, total, splitTip, splitTotal, tax, taxRate, defTaxRate;
    public int people, defPeople, percent_array_length, spinnerPosition;
    public int defaultIndex = 0;
    boolean recalculate = true;
    boolean rounding = false;
    boolean isUnevenShown = false;
    boolean enteredTaxRate = false;
    boolean recalculateTax = true;
    float scaleValue;

    public CalculatorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        main = getActivity();

        rootView = inflater.inflate(R.layout.fragment_calculator, container, false);

        scaleValue = rootView.getContext().getResources().getDisplayMetrics().density;

        prefUnitKey = main.getString(R.string.pref_unit_key);
        prefPeopleKey = main.getString(R.string.pref_people_key);
        prefPercentsKey = main.getString(R.string.pref_percents_key);
        prefDefaultKey = main.getString(R.string.pref_default_key);
        prefTaxKey = main.getString(R.string.pref_tax_key);

        spinner = (AppCompatSpinner) rootView.findViewById(R.id.percent_amount_spinner);
        adapter = new ArrayAdapter<>(main, R.layout.spinner_item, percentsArray);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setOnItemSelectedListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(main);
        setUnitPreference(preferences);
        setSpinnerPreferences(preferences);
        setPeoplePreference(preferences);
        setTaxPreference(preferences);

        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(prefPeopleKey)) {
                    setPeoplePreference(sharedPreferences);
                } else if (key.equals(prefUnitKey)) {
                    setUnitPreference(sharedPreferences);
                } else if (key.equals(prefPercentsKey)) {
                    setSpinnerPreferences(sharedPreferences);
                } else if (key.equals(prefDefaultKey)) {
                    setDefaultPreference(sharedPreferences);
                } else if (key.equals(prefTaxKey)) {
                    setTaxPreference(sharedPreferences);
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(prefListener);

        scroll_view = rootView.findViewById(R.id.scroll_view);
        calculator_card = rootView.findViewById(R.id.calculator_card);
        sale_text = rootView.findViewById(R.id.sale_amount);
        tip_text = rootView.findViewById(R.id.tip_amount);
        total_text = rootView.findViewById(R.id.total_amount);
        split_card = rootView.findViewById(R.id.split_card);
        fab_plus = rootView.findViewById(R.id.fab_plus);
        people_count = rootView.findViewById(R.id.people_count);
        split_tip = rootView.findViewById(R.id.split_tip);
        split_total = rootView.findViewById(R.id.split_total);
        even_content = rootView.findViewById(R.id.even_split_content);
        uneven_content = rootView.findViewById(R.id.uneven_split_content);
        people_layout = (TableLayout) rootView.findViewById(R.id.people_list);
        sales_tax = rootView.findViewById(R.id.sales_tax);

        setCalcListeners();
        createPersonArray();

        split_card.animate().alpha(0f).translationY(-100f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            removeFABPlusOutline(fab_plus);
        }

        setButtons(rootView);

        return rootView;
    }

    private void setPeoplePreference(SharedPreferences sp) {
        peoplePref = sp.getString(
                main.getString(R.string.pref_people_key),
                main.getString(R.string.pref_people_default));
        if (!peoplePref.matches("\\d+") || Integer.parseInt(peoplePref) < 2) {
            peoplePref = main.getString(R.string.pref_people_default);
        }
        defPeople = Integer.parseInt(peoplePref);
        people = defPeople;
    }

    public void setUnitPreference(SharedPreferences sp) {
        unitSymbol = sp.getString(
                main.getString(R.string.pref_unit_key),
                main.getString(R.string.pref_unit_default));
        ((TextView) rootView.findViewById(R.id.dollar1)).setText(unitSymbol);
        ((TextView) rootView.findViewById(R.id.dollar2)).setText(unitSymbol);
        ((TextView) rootView.findViewById(R.id.dollar3)).setText(unitSymbol);
        ((TextView) rootView.findViewById(R.id.dollar4)).setText(unitSymbol);
    }

    private void setSpinnerPreferences(SharedPreferences sp) {
        defaultPercentsArray.clear();
        percentString = sp.getString(
                main.getString(R.string.pref_percents_key),
                main.getString(R.string.pref_percents_default));

        // If user did not enter in any valid numbers, resort to default.
        if (!percentString.matches(".*\\d.*")) {
            percentString = main.getString(R.string.pref_percents_default);
        }

        while (percentString.contains(",") && percentString.matches(".*\\d.*")) {

            addPercentString = percentString.substring(0, percentString.indexOf(","));

            if (addPercentString.matches("\\d+") && Integer.parseInt(addPercentString) != 0) {
                defaultPercentsArray.add(addPercentString + "%");
            }
            percentString = percentString.substring(percentString.indexOf(",") + 1);

        }

        // Adding in the last number that is not bordered by a comma.
        if (!percentString.isEmpty() && percentString.matches("\\d+") && Integer.parseInt(addPercentString) != 0) {
            defaultPercentsArray.add(percentString + "%");
        }

        setDefaultPreference(sp);
    }

    private void setDefaultPreference(SharedPreferences sp) {
        defaultPercent = sp.getString(main.getString(R.string.pref_default_key), main.getString(R.string.pref_default_default)) + "%";

        toSpinnerPercentsArray.clear();
        toSpinnerPercentsArray.addAll(defaultPercentsArray);

        if (defaultPercentsArray.indexOf(defaultPercent) < 0) {
            toSpinnerPercentsArray.add(defaultPercent);
        }

        Collections.sort(toSpinnerPercentsArray, new PercentComparator());
        defaultIndex = toSpinnerPercentsArray.indexOf(defaultPercent);

        toSpinnerPercentsArray.add("Custom");

        percent_array_length = toSpinnerPercentsArray.size();

        populateSpinner();
    }

    private void setTaxPreference(SharedPreferences sp) {
        defTaxRate = Double.parseDouble(sp.getString(main.getString(R.string.pref_tax_key), main.getString(R.string.pref_tax_default))) / 100;

        if (defTaxRate != Double.parseDouble(main.getString(R.string.pref_tax_default))) {
            enteredTaxRate = true;
        } else {
            enteredTaxRate = false;
        }

        taxRate = defTaxRate;
    }

    private void populateSpinner() {
        percentsArray.clear();
        percentsArray.addAll(toSpinnerPercentsArray);
        spinner.setAdapter(adapter);
        spinner.setSelection(defaultIndex);
    }

    private void setCalcListeners() {

        final TextView sale_view = (TextView) sale_text;
        sale_view.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        sale = Double.parseDouble(((TextView) sale_text).getText().toString());
                    } else {
                        sale = 0;
                    }
                    saleChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        sale_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    hideKeyboard();
                    sale_view.clearFocus();
                    spinner.requestFocus();
                    spinner.performClick();
                }
                return true;
            }

        });

        ((TextView) tip_text).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        tip = Double.parseDouble(((TextView) tip_text).getText().toString());
                    } else {
                        tip = 0;
                    }
                    tipChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ((TextView) total_text).addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                try {
                    if (s.length() != 0) {
                        total = Double.parseDouble(((TextView) total_text).getText().toString());
                    } else {
                        total = 0;
                    }
                    totalChanged();
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        ((TextView) sales_tax).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (recalculateTax) {
                        if (s.length() != 0) {
                            tax = Double.parseDouble(((TextView) sales_tax).getText().toString());

                            calculateTaxRate();
                        } else {
                            resetTaxRate();
                        }
                    }
                } catch (NumberFormatException e) {
//                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });
    }

    private void setButtons(View rootView) {

        final Animation rotateX = AnimationUtils.loadAnimation(main, R.anim.fab_rotate_to_x);
        final Animation rotatePlus = AnimationUtils.loadAnimation(main, R.anim.fab_rotate_to_plus);

        final Button clear_button = (Button) rootView.findViewById(R.id.clear_button);
        clear_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                recalculate = false;
                ((ScrollView) scroll_view).smoothScrollTo(0, 0);

                ((TextView) sale_text).setText("");
                ((TextView) tip_text).setText("");
                ((TextView) total_text).setText("");
                calcSplits();
                sale = 0;
                populateSpinner();
                tip = 0;
                total = 0;
                people = defPeople;
                createPersonArray();
                resetTaxRate();
                recalculate = true;

                if (split_card.isShown())
                    hideCard(rotatePlus);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    clearReveal(clear_button);
                }
            }
        });

        Button round_button = (Button) rootView.findViewById(R.id.round_button);
        round_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (total != 0) {
                    ((TextView) total_text).setText(String.format("%.2f", (double) Math.round(total)));
                    rounding = true;
                    if (even_content.isShown()) {
                        calcSplits();
                    }
                }
            }
        });


        Button split_button = (Button) rootView.findViewById(R.id.split_button);
        split_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (split_card.isShown()) {
                    hideCard(rotatePlus);
                } else {
                    showCard(rotateX);
                }
            }
        });

        Button add_people = (Button) rootView.findViewById(R.id.add_people);
        Button take_people = (Button) rootView.findViewById(R.id.take_people);

        add_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                people++;
                addPersonToList();
                setPeople();
                calcSplits();
            }
        });

        take_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (people > 2) {
                    people--;
                    removePersonFromList();
                } else {
                    hideCard(rotatePlus);
                    people = defPeople;
                }
                setPeople();
                calcSplits();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (recalculate && !rounding) {
            String percent_number = parent.getItemAtPosition(position).toString();
            percent_number = percent_number.substring(0, percent_number.length() - 1);
            try {
                percent = Double.parseDouble(percent_number);
                percentChanged();
                spinnerPosition = position;
            } catch (NumberFormatException e) {
                if (percent_number.equals("Custo")) {
                    showPercentDialog();
                }
            }
        }

        // Remove messy recalculated or custom percentages upon selection of default ones.
        if (percentsArray.size() > percent_array_length) {
            percentsArray.remove(percentsArray.size() - 1);
        }

        rounding = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void addPercentToSpinner(double p, boolean r) {
        rounding = r;
        spinner.setAdapter(adapter);
        if (p == 0) {
            if (spinnerPosition >= percentsArray.size()) spinnerPosition = defaultIndex;
            spinner.setSelection(spinnerPosition);
        } else {
            if (percentsArray.size() > percent_array_length) {
                percentsArray.remove(percentsArray.size() - 1);
            }
            percentsArray.add(String.format("%.1f", p) + "%");
            spinner.setSelection(percentsArray.size() - 1);
            spinnerPosition = percentsArray.size() - 1;
        }
//        rounding = false;
    }

    private void showPercentDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        PercentDialogFragment percentDialog = new PercentDialogFragment();
        percentDialog.setTargetFragment(this, 0);
        percentDialog.show(fm, "custom_percent_dialog");
    }

    private void calculateTaxRate() {
//        && !enteredTaxRate
        if (sale != 0 && taxRate == 0) {
            taxRate = tax / (sale - tax); //Todo: Entering in a value to calculate taxRate is inaccurate?
        }
        Log.d(LOG_TAG, "Tax rate: " + Double.toString(taxRate));
    }

    private void resetTaxRate() {
        taxRate = defTaxRate;
        setSalesTax();
    }

    private void setSalesTax() {
        recalculateTax = false;
        if (sale != 0 && taxRate != 0) {
            tax = (sale * taxRate) / (1 + taxRate);
            ((TextView) sales_tax).setText(String.format("%.2f", tax));
        } else {
            tax = 0;
            ((TextView) sales_tax).setText("");
        }
        recalculateTax = true;
    }

    private void saleChanged() {
        if (percent != 0 && recalculate) {
            recalculate = false;
            tip = sale * (percent / 100);
            total = sale + tip;

            calculateTaxRate();
            setSalesTax();

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            ((TextView) total_text).setText(String.format("%.2f", total));

            recalculate = true;
        }

        if (even_content.isShown()) {
            calcSplits();
        }

    }

    private void percentChanged() {
        if (sale != 0 && recalculate) {
            recalculate = false;
            tip = sale * (percent / 100);
            total = sale + tip;

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            ((TextView) total_text).setText(String.format("%.2f", total));

            recalculate = true;
        }

        if (even_content.isShown()) {
            calcSplits();
        }
    }

    private void tipChanged() {
        if (sale != 0 && recalculate) {
            recalculate = false;
            percent = (tip / sale) * 100;
            total = sale + tip;


            addPercentToSpinner(percent, true);
            ((TextView) total_text).setText(String.format("%.2f", total));

            recalculate = true;
        }

        if (even_content.isShown()) {
            calcSplits();
        }
    }

    private void totalChanged() {
        if (sale != 0 && recalculate && total > sale) {
            recalculate = false;
            tip = total - sale;
            percent = (tip / sale) * 100;

            ((TextView) tip_text).setText(String.format("%.2f", tip));
            addPercentToSpinner(percent, true);

            recalculate = true;
        }

        if (even_content.isShown()) {
            calcSplits();
        }

    }

    private void setPeople() {
        ((TextView) people_count).setText(Integer.toString(people) + getString(R.string.people));
    }

    private void calcSplits() {
        if (rootView.findViewById(R.id.even_split_content).getVisibility() == View.VISIBLE) {
            splitTip = tip / people;
            splitTotal = total / people;

            ((TextView) split_tip).setText(unitSymbol + String.format("%.2f", splitTip) + rootView.getResources().getString(R.string.tip_per));
            ((TextView) split_total).setText(unitSymbol + String.format("%.2f", splitTotal));
        }
    }

    private void createPersonArray() {
        peopleList.clear();
        people_layout.removeAllViews();

        LayoutInflater LI = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LI.inflate(R.layout.table_row_title, people_layout);

        for (int i = 0; i < defPeople; i++) {
            addPersonToList();
        }
    }

    private void addPersonToList() {
        peopleList.add(new Person());

        LayoutInflater LI = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = LI.inflate(R.layout.table_row_person, null);

        TextView numbertv = (TextView) v.findViewById(R.id.person_number);
        numbertv.setText(Integer.toString(peopleList.size()));

        final TextView totaltv = (TextView) v.findViewById(R.id.person_total);
        totaltv.setText(unitSymbol + "0.00");

        //Todo: Move to a function so can recall multiple times upon updating of other values (taxrate and percent).
        //Todo: Calculate last subtotal based on the sale value???
        final MaterialEditText subtotalv = ((MaterialEditText) v.findViewById(R.id.person_subtotal));
        subtotalv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    double personTotal = Double.parseDouble(s.toString());
                    personTotal = personTotal * (1+taxRate);
                    personTotal = (personTotal * (1 + (percent / 100)));
                    totaltv.setText(unitSymbol + String.format("%.2f", personTotal));
                } catch (NumberFormatException e) {

                }
            }
        });

        people_layout.addView(v);
    }

    private void removePersonFromList() {
        people_layout.removeViewAt(people_layout.getChildCount() - 1);
        peopleList.remove(peopleList.size() - 1);
    }

    private void showCard(Animation animation) {
        fab_plus.startAnimation(animation);
        split_card.setVisibility(View.VISIBLE);
        split_card.animate().alpha(1f).translationY(0f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ((ScrollView) scroll_view).smoothScrollTo(0, split_card.getBottom());
            }
        });
        setPeople();
        calcSplits();
    }

    private void hideCard(Animation animation) {
        fab_plus.startAnimation(animation);
        split_card.animate().alpha(0f).translationY(-100f).setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                ((ScrollView) scroll_view).smoothScrollTo(0, 0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                split_card.setVisibility(View.GONE);
            }
        });
    }

    @TargetApi(21) // Remove the drop shadow from the + drawable
    private void removeFABPlusOutline(View plusView) {
        ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                view.setOutlineProvider(null);
            }
        };
        plusView.setOutlineProvider(viewOutlineProvider);
    }

    @TargetApi(21) // Material ripple to clear the card
    private void clearReveal(Button source) {
        LinearLayout.LayoutParams shift = (LinearLayout.LayoutParams) calculator_card.getLayoutParams();

        int centerX = ((source.getLeft() + source.getRight()) / 2)
                + shift.leftMargin;
        int centerY = (calculator_card.getBottom() - (source.getHeight() / 2))
                - (shift.topMargin + calculator_card.getPaddingBottom());
        int radius = (int) Math.sqrt(Math.pow(calculator_card.getWidth(), 2) + Math.pow(calculator_card.getHeight(), 2));

        Animator reveal = ViewAnimationUtils.createCircularReveal(
                calculator_card,
                centerX,
                centerY,
                0,
                radius);
        reveal.setDuration(400);
        reveal.start();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) main.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(main.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroy() {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(prefListener);
    }
}