#ifdef CPU_DYNARMIC
#include "cpu_dynarmic.hpp"
#include "arm_defs.hpp"
#include "emulator.hpp"

CPU::CPU(Memory& mem, Kernel& kernel, Emulator& emu) 
    : mem(mem), emu(emu), scheduler(emu.getScheduler()), env(mem, kernel, scheduler) {
    cp15 = std::make_shared<CP15>();

    Dynarmic::A32::UserConfig config;
    config.arch_version = Dynarmic::A32::ArchVersion::v6K;
    config.callbacks = &env;
    config.coprocessors[15] = cp15;
    config.define_unpredictable_behaviour = true;
    config.global_monitor = &exclusiveMonitor;
    config.processor_id = 0;

    jit = std::make_unique<Dynarmic::A32::Jit>(config);
}

void CPU::reset() {
    setCPSR(CPSR::UserMode);
    setFPSCR(FPSCR::MainThreadDefault);

    cp15->reset();
    cp15->setTLSBase(VirtualAddrs::TLSBase);  
    jit->Reset();
    jit->ClearCache();
    jit->Regs().fill(0);
    jit->ExtRegs().fill(0);
}

void CPU::runFrame() {
    bool frameDone = false;
    while (!frameDone) {
        env.ticksLeft = scheduler.nextTimestamp - scheduler.currentTimestamp;
        const auto exitReason = jit->Run();
        emu.pollScheduler();
        
        if (static_cast<u32>(exitReason) != 0) {
            if (Dynarmic::Has(exitReason, Dynarmic::HaltReason::CacheInvalidation)) {
                continue; // Retry execution
            } else {
                Helpers::panic("Exit reason: %d\nPC: %08X", static_cast<u32>(exitReason), getReg(15));
            }
        }
        frameDone = emu.frameDone;
    }
}

#endif
