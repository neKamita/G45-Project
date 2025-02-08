package uz.pdp.enums;

import lombok.Getter;

@Getter
public enum Size {
    SIZE_200x2000(200, 2000),
    SIZE_300x2000(300, 2000),
    SIZE_400x2000(400, 2000),
    SIZE_500x2000(500, 2000),
    SIZE_600x2000(600, 2000),
    SIZE_700x2000(700, 2000),
    SIZE_800x2000(800, 2000),
    SIZE_900x2000(900, 2000),
    SIZE_1000x2000(1000, 2000),
    SIZE_1100x2000(1100, 2000),
    SIZE_1200x2000(1200, 2000),
    CUSTOM(0, 0);

    private final int width;
    private final int height;

    Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Object getDisplayName() {
        return this.name();
    }
}