module MiniPlanes {
    requires javafx.controls;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires commons.math3;

    exports mainapplication;
    exports view;
    exports viewmodel;
}