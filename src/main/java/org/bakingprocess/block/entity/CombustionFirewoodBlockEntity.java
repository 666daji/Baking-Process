package org.bakingprocess.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bakingprocess.block.CombustionFirewoodBlock;
import org.bakingprocess.block.FirewoodBlock;
import org.bakingprocess.registry.ModBlockEntityTypes;

public class CombustionFirewoodBlockEntity extends BlockEntity {
    protected int energy;
    protected int cycleCount; // 寰�鐜�娆℃暟
    protected boolean isFirstCycle = true; // 鏄�鍚︽槸棣栨�″惊鐜?
    /** 鐑�閲忕瓑绾э�?-鏃犵儹閲忥紝1-浣庣儹閲忥紝2-楂樼儹閲?*/
    protected int heatLevel = 0;

    static final int MAX_ENERGY = 12000;
    static final int HALF_ENERGY = MAX_ENERGY / 2; // 50%鑳介噺闃堝�?
    static final int FIREWOOD_ENERGY = HALF_ENERGY; // 姣忔�℃坊鏌村�炲姞50%鑳介噺

    public CombustionFirewoodBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.COMBUSTION_FIREWOOD.get(), pos, state);
        // 棣栨�＄偣鐕冩椂璁剧疆婊¤兘閲�
        if (state.getValue(CombustionFirewoodBlock.COMBUSTION_STATE) == CombustionFirewoodBlock.CombustionState.FIRST_IGNITED) {
            this.energy = MAX_ENERGY;
            this.isFirstCycle = true;
            this.cycleCount = 0;
        }
        updateHeatLevel();
    }

    public static void tick(Level world, BlockPos pos, BlockState state, CombustionFirewoodBlockEntity blockEntity) {
        // 姣弔ick娑堣�?鐐硅兘閲?
        blockEntity.consumeEnergy();
        blockEntity.checkClearSpaceAbove(world, pos, state);

        // 濡傛灉鑳介噺鑰楀敖涓斿�勪簬鐕冪儳鐘舵�侊紝鏇存柊鍒扮噧灏界姸鎬?
        if (blockEntity.energy <= 0 && state.getValue(CombustionFirewoodBlock.COMBUSTION_STATE).isBurning()) {
            blockEntity.updateCombustionState();
        }
    }

    /**
     * 寮哄埗鐔勭伃褰撳墠鐕冪儳鐨勬煷鐏�鍫�
     * 鏍规嵁褰撳墠鏂瑰潡鐘舵�佸喅瀹氱唲鐏�鍚庣殑鐘舵�?
     * @return 鏄�鍚︽垚鍔熺唲鐏�锛堝�傛灉宸茬粡鐔勭伃鍒欒繑鍥瀎alse锛?
     */
    public boolean extinguish() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        BlockState currentState = getBlockState();
        CombustionFirewoodBlock.CombustionState currentCombustionState =
                currentState.getValue(CombustionFirewoodBlock.COMBUSTION_STATE);

        // 濡傛灉宸茬粡澶勪簬鐔勭伃鐘舵�侊紝涓嶉渶瑕佸啀娆＄唲鐏?
        if (!currentCombustionState.isBurning()) {
            return false;
        }

        CombustionFirewoodBlock.CombustionState extinguishedState = switch (currentCombustionState) {
            case FIRST_IGNITED, FIRST_HALF -> CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED;
            case AGAIN_IGNITED, AGAIN_HALF, REIGNITED -> CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED;
            default ->
                // 榛樿�ゆ儏鍐典笅锛屽�傛灉鏄�棣栨�＄噧鐑у垯鐔勭伃涓洪�栨�＄噧灏斤紝鍚﹀垯涓洪潪棣栨�＄噧灏�
                    isFirstCycle ?
                            CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED :
                            CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED;
        };

        this.energy = 0;
        level.setBlockAndUpdate(worldPosition, currentState.setValue(CombustionFirewoodBlock.COMBUSTION_STATE, extinguishedState));
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.0f);
        spawnExtinguishParticles(level, worldPosition);

        setChanged();
        return true;
    }

    /**
     * 娣绘煷鎿嶄綔 - 浠讳綍鑳介噺涓嶆弧鐨勭姸鎬侀兘鍙�浠ユ坊鏌�
     * @return 鏄�鍚︽垚鍔熸坊鏌�
     */
    public boolean addFirewood() {
        if (energy >= MAX_ENERGY || isCompletelyExtinguished()) {
            return false; // 鑳介噺宸叉弧鎴栧畬鍏ㄧ噧灏斤紝鏃犳硶娣绘煷
        }

        CombustionFirewoodBlock.CombustionState currentState = getBlockState().getValue(CombustionFirewoodBlock.COMBUSTION_STATE);
        CombustionFirewoodBlock.CombustionState newState;

        // 鏍规嵁褰撳墠鐘舵�佸喅瀹氭坊鏌村悗鐨勭姸鎬?
        switch (currentState) {
            case FIRST_IGNITED:
            case FIRST_HALF:
            case FIRST_EXTINGUISHED, REIGNITED, AGAIN_EXTINGUISHED:
                newState = CombustionFirewoodBlock.CombustionState.AGAIN_IGNITED;
                break;

            case AGAIN_IGNITED:
            case AGAIN_HALF:
                newState = CombustionFirewoodBlock.CombustionState.REIGNITED;
                break;

            default:
                return false;
        }

        // 澧炲姞50%鑳介噺
        addEnergy(FIREWOOD_ENERGY);

        // 鏇存柊鏂瑰潡鐘舵�?
        if (level != null) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CombustionFirewoodBlock.COMBUSTION_STATE, newState));
        }
        setChanged();
        return true;
    }

    /**
     * 鏇存柊鐑�閲忕瓑绾�
     */
    public void updateHeatLevel() {
        int oldHeatLevel = heatLevel;

        if (energy <= 0) {
            heatLevel = 0; // 鏈�鐕冪儳鎴栬兘閲忚�楀敖锛屾棤鐑�閲�
        } else if (energy > HALF_ENERGY) {
            heatLevel = 2; // 鑳介噺澶т簬50%锛岄珮鐑�閲�
        } else {
            heatLevel = 1; // 鑳介噺灏忎簬绛変簬50%锛屼綆鐑�閲�
        }

        // 濡傛灉鐑�閲忕瓑绾ф敼鍙橈紝鏍囪�伴渶瑕佸悓姝?
        if (oldHeatLevel != heatLevel) {
            setChanged();
        }
    }

    /**
     * 鑾峰彇鐑�閲忕瓑绾�
     */
    public int getHeatLevel() {
        return heatLevel;
    }

    /**
     * 妫�鏌ユ槸鍚︽湁鐑�閲�
     */
    public boolean hasHeat() {
        return heatLevel > 0;
    }

    /**
     * 鏇存柊鐕冪儳鐘舵�?
     */
    public void updateCombustionState() {
        if (level == null || level.isClientSide()) return;

        CombustionFirewoodBlock.CombustionState currentState = getBlockState().getValue(CombustionFirewoodBlock.COMBUSTION_STATE);
        CombustionFirewoodBlock.CombustionState newState = currentState;

        // 鏍规嵁褰撳墠鐘舵�佸拰鑳介噺鍊煎喅瀹氫笅涓�涓�鐘舵�?
        if (energy > 0) {
            // 鐕冪儳鐘舵�佽浆鎹?
            switch (currentState) {
                case FIRST_IGNITED:
                    if (energy <= HALF_ENERGY) {
                        newState = CombustionFirewoodBlock.CombustionState.FIRST_HALF;
                    }
                    break;

                case FIRST_HALF:
                    // 淇濇寔鍦‵IRST_HALF鐩村埌鑳介噺涓?
                    break;

                case AGAIN_IGNITED:
                    if (energy <= HALF_ENERGY) {
                        newState = CombustionFirewoodBlock.CombustionState.AGAIN_HALF;
                    }
                    break;

                case AGAIN_HALF:
                    // 淇濇寔鍦ˋGAIN_HALF鐩村埌鑳介噺涓?
                    break;

                case REIGNITED:
                    if (energy <= HALF_ENERGY) {
                        newState = CombustionFirewoodBlock.CombustionState.FIRST_HALF;
                        isFirstCycle = false; // 棣栨�″惊鐜�缁撴潫
                        cycleCount++;
                    }
                    break;

                default:
                    break;
            }
        } else {
            // 鑳介噺涓?鏃剁殑鐕冨敖鐘舵�?
            switch (currentState) {
                case FIRST_HALF:
                    newState = CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED;
                    break;

                case AGAIN_HALF:
                    newState = CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED;
                    break;

                default:
                    break;
            }
        }

        // 鍙�鏈夊綋鐘舵�佺‘瀹炴敼鍙樻椂鎵嶆洿鏂帮紝閬垮厤涓嶅繀瑕佺殑鏂瑰潡鏇存柊
        if (currentState != newState) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CombustionFirewoodBlock.COMBUSTION_STATE, newState));
        }

        // 鏇存柊鐑�閲忕瓑绾�
        updateHeatLevel();

        setChanged();
    }

    /**
     * 鐢熸垚鐔勭伃绮掑瓙鏁堟灉
     */
    private void spawnExtinguishParticles(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            // 瀹㈡埛绔�鐢熸垚鐑熼浘绮掑�?
            RandomSource random = world.random;
            for (int i = 0; i < 5; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5;
                double y = pos.getY() + 0.3 + random.nextDouble() * 0.2;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5;

                world.addParticle(ParticleTypes.SMOKE,
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.05,
                        0.05,
                        (random.nextDouble() - 0.5) * 0.05);
            }
        }
    }

    /**
     * 妫�鏌ヤ笂鏂圭┖闂达紝濡傛灉涓嶆弧瓒虫潯浠跺垯寮哄埗鐔勭伃
     */
    private void checkClearSpaceAbove(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide()) return;

        // 鍙�鏈夊湪鐕冪儳鐘舵�佷笅鎵嶉渶瑕佹��鏌?
        CombustionFirewoodBlock.CombustionState currentState = state.getValue(CombustionFirewoodBlock.COMBUSTION_STATE);
        if (!currentState.isBurning()) return;

        // 妫�鏌ヤ笂鏂圭┖闂?
        if (!FirewoodBlock.hasClearSpaceAbove(world, pos)) {
            // 涓婃柟绌洪棿琚�闃诲�烇紝寮哄埗鐔勭伃
            extinguishDueToObstruction(world, pos);
        }
    }

    /**
     * 鐢变簬涓婃柟闃诲�炶�屽己鍒剁唲鐏?
     */
    private void extinguishDueToObstruction(Level world, BlockPos pos) {
        BlockState currentState = getBlockState();
        CombustionFirewoodBlock.CombustionState currentCombustionState =
                currentState.getValue(CombustionFirewoodBlock.COMBUSTION_STATE);

        CombustionFirewoodBlock.CombustionState extinguishedState = switch (currentCombustionState) {
            case FIRST_IGNITED, FIRST_HALF -> CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED;
            case AGAIN_IGNITED, AGAIN_HALF, REIGNITED -> CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED;
            default -> isFirstCycle ?
                    CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED :
                    CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED;
        };
        // 璁剧疆鑳介噺涓?
        this.energy = 0;

        // 鏇存柊鏂瑰潡鐘舵�?
        world.setBlockAndUpdate(pos, currentState.setValue(CombustionFirewoodBlock.COMBUSTION_STATE, extinguishedState));

        // 鎾�鏀剧壒娈婄殑闃诲�炵唲鐏�闊虫�?
        world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 0.8f);

        // 鐢熸垚鏇村�氱殑鐑熼浘绮掑瓙锛岃〃绀哄洜闃诲�炶�岀唲鐏?
        spawnObstructionExtinguishParticles(world, pos);

        setChanged();
    }

    /**
     * 鐢熸垚鍥犻樆濉炶�岀唲鐏�鐨勭矑瀛愭晥鏋�
     */
    private void spawnObstructionExtinguishParticles(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            RandomSource random = world.random;
            // 鐢熸垚鏇村�氱殑鐑熼浘绮掑�?
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;

                world.addParticle(ParticleTypes.SMOKE,
                        x, y, z,
                        (random.nextDouble() - 0.5) * 0.1,
                        0.05 + random.nextDouble() * 0.1,
                        (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }

    /**
     * 妫�鏌ユ槸鍚﹀彲浠ユ坊鏌?
     */
    public boolean canAddFirewood() {
        return energy < MAX_ENERGY && !isCompletelyExtinguished();
    }

    /**
     * 妫�鏌ユ槸鍚︽�ｅ湪鐕冪�?
     */
    public boolean isCombusting() {
        return this.heatLevel > 0;
    }

    public void addEnergy(int energy) {
        this.energy = Math.min(this.energy + energy, MAX_ENERGY);
        updateHeatLevel();
        updateCombustionState();
    }

    public boolean consumeEnergy() {
        return consumeEnergy(1);
    }

    /**
     * 娑堣�楁寚瀹氭暟閲忕殑鑳介噺
     * @param amount 娑堣�楃殑鑳介噺鏁伴噺
     * @return 鏄�鍚︽垚鍔熸秷鑰椾簡鑳介噺
     */
    public boolean consumeEnergy(int amount) {
        if (energy <= 0 || amount <= 0) {
            return false;
        }

        int oldEnergy = this.energy;
        this.energy = Math.max(0, this.energy - amount);

        // 鍙�鏈夊綋鑳介噺纭�瀹炴敼鍙樻椂鎵嶆洿鏂扮姸鎬?
        if (oldEnergy != this.energy) {
            updateHeatLevel();
            updateCombustionState();
            return true;
        }

        return false;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
        updateHeatLevel();
        updateCombustionState();
    }


    /**
     * 妫�鏌ヨ兘閲忔槸鍚﹀凡缁忚�楀敖
     * @return 鑳介噺鏄�鍚�<=0
     */
    public boolean isEnergyDepleted() {
        return energy <= 0;
    }

    /**
     * 鑾峰彇褰撳墠鑳介噺鍊?
     * @return 褰撳墠鑳介噺鍊?
     */
    public int getCurrentEnergy() {
        return energy;
    }

    /**
     * 鑾峰彇鑳介噺娑堣�楄繘搴︼紙0.0鍒?.0锛?
     * @return 鑳介噺娑堣�楄繘搴︼紝0琛ㄧず婊¤兘閲忥紝1琛ㄧず鑳介噺鑰楀敖
     */
    public float getEnergyConsumptionProgress() {
        return 1.0f - ((float) energy / MAX_ENERGY);
    }

    public int getEnergy() {
        return energy;
    }

    public static int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(int count) {
        this.cycleCount = count;
        setChanged();
    }

    public boolean isFirstCycle() {
        return isFirstCycle;
    }

    public void setFirstCycle(boolean firstCycle) {
        this.isFirstCycle = firstCycle;
        setChanged();
    }

    /**
     * 鑾峰彇褰撳墠鑳介噺鐧惧垎姣?
     */
    public float getEnergyRatio() {
        return (float) energy / MAX_ENERGY;
    }

    /**
     * 妫�鏌ユ槸鍚﹀畬鍏ㄧ噧灏斤紙涓嶈兘鍐嶇噧鐑э級
     */
    public boolean isCompletelyExtinguished() {
        CombustionFirewoodBlock.CombustionState currentState = getBlockState().getValue(CombustionFirewoodBlock.COMBUSTION_STATE);
        return (currentState == CombustionFirewoodBlock.CombustionState.FIRST_EXTINGUISHED ||
                currentState == CombustionFirewoodBlock.CombustionState.AGAIN_EXTINGUISHED) &&
                energy <= 0;
    }

    /**
     * 鑾峰彇鍗婅兘閲忓�硷紙50%锛?
     */
    public int getHalfEnergy() {
        return HALF_ENERGY;
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("Energy", energy);
        nbt.putInt("CycleCount", cycleCount);
        nbt.putBoolean("IsFirstCycle", isFirstCycle);
        nbt.putInt("HeatLevel", heatLevel); // 淇濆瓨鐑�閲忕瓑绾�
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        energy = nbt.getInt("Energy");
        cycleCount = nbt.getInt("CycleCount");
        isFirstCycle = nbt.getBoolean("IsFirstCycle");
        heatLevel = nbt.getInt("HeatLevel"); // 璇诲彇鐑�閲忕瓑绾�
        // 濡傛灉NBT涓�娌℃湁鐑�閲忕瓑绾э紝鏍规嵁鑳介噺璁＄畻
        if (!nbt.contains("HeatLevel")) {
            updateHeatLevel();
        }
    }
}