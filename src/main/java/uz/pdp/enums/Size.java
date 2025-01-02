package uz.pdp.enums;

public enum Size {
    STANDARD(200, 200),     
    MEDIUM(220, 220),     
    LARGE(240, 240),        
    CUSTOM(0, 0);
    
    private final int width;
    private final int height;
    
    Size(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}